package com.rpl.manager;

import com.rpl.client.dto.SuspensionResponse;
import com.rpl.domain.ActionStatus;
import com.rpl.domain.ImplementedAction;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.Suspension;
import com.rpl.domain.state.AbandonedState;
import com.rpl.domain.state.ActionState;
import com.rpl.domain.state.CompletedState;
import com.rpl.domain.state.InProgressState;
import com.rpl.domain.state.ProposedState;
import com.rpl.domain.state.ReviewingState;
import com.rpl.domain.state.SuspendedState;
import com.rpl.engine.LedgerEntryEngine;
import com.rpl.exception.ConflictException;
import com.rpl.exception.NotFoundException;
import com.rpl.exception.ValidationException;
import com.rpl.resourceaccess.ImplementedActionRepository;
import com.rpl.resourceaccess.ProposedActionRepository;
import com.rpl.resourceaccess.SuspensionRepository;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ActionManager {
    private final ProposedActionRepository proposedActionRepository;
    private final ImplementedActionRepository implementedActionRepository;
    private final SuspensionRepository suspensionRepository;
    private final LedgerEntryEngine ledgerEntryEngine;
    private final AuditLogManager auditLogManager;
    private final Clock clock;
    private final Map<ActionStatus, ActionState> states;

    public ActionManager(
            ProposedActionRepository proposedActionRepository,
            ImplementedActionRepository implementedActionRepository,
            SuspensionRepository suspensionRepository,
            LedgerEntryEngine ledgerEntryEngine,
            AuditLogManager auditLogManager,
            Clock clock,
            ProposedState proposedState,
            SuspendedState suspendedState,
            InProgressState inProgressState,
            ReviewingState reviewingState,
            CompletedState completedState,
            AbandonedState abandonedState) {
        this.proposedActionRepository = proposedActionRepository;
        this.implementedActionRepository = implementedActionRepository;
        this.suspensionRepository = suspensionRepository;
        this.ledgerEntryEngine = ledgerEntryEngine;
        this.auditLogManager = auditLogManager;
        this.clock = clock;
        this.states = Map.of(
                ActionStatus.PROPOSED, proposedState,
                ActionStatus.SUSPENDED, suspendedState,
                ActionStatus.IN_PROGRESS, inProgressState,
                ActionStatus.REVIEWING, reviewingState,
                ActionStatus.COMPLETED, completedState,
                ActionStatus.ABANDONED, abandonedState
        );
    }

    public ProposedAction get(Long id) {
        return proposedActionRepository.findById(id).orElseThrow(() -> new NotFoundException("Action not found"));
    }

    public ProposedAction transition(Long id, String event) {
        return transition(id, event, "");
    }

    public ProposedAction suspendWithReason(Long id, String reason) {
        return transition(id, "suspend", reason != null ? reason : "");
    }

    public ProposedAction reviewAction(Long id) {
        ProposedAction action = proposedActionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Action not found"));
        if (action.getStatus() != ActionStatus.IN_PROGRESS) {
            throw new ConflictException("review requires action in IN_PROGRESS state");
        }
        ActionStatus next = states.get(ActionStatus.IN_PROGRESS).review(action);
        action.setStatus(next);
        ProposedAction saved = proposedActionRepository.save(action);
        auditLogManager.record("ACTION_REVIEWED", null, null, saved.getId());
        return saved;
    }

    public ProposedAction approveAction(Long id) {
        ProposedAction action = proposedActionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Action not found"));
        if (action.getStatus() != ActionStatus.REVIEWING) {
            throw new ConflictException("approve requires action in REVIEWING state");
        }
        ActionStatus next = states.get(ActionStatus.REVIEWING).approve();
        action.setStatus(next);
        ProposedAction saved = proposedActionRepository.save(action);
        implementedActionRepository.findByProposedAction_Id(saved.getId()).ifPresent(ledgerEntryEngine::generate);
        auditLogManager.record("ACTION_APPROVED", null, null, saved.getId());
        return saved;
    }

    public ProposedAction rejectAction(Long id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("Rejection reason is required");
        }
        ProposedAction action = proposedActionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Action not found"));
        if (action.getStatus() != ActionStatus.REVIEWING) {
            throw new ConflictException("reject requires action in REVIEWING state");
        }
        ActionStatus next = states.get(ActionStatus.REVIEWING).reject(action, reason);
        action.setStatus(next);
        ProposedAction saved = proposedActionRepository.save(action);
        auditLogManager.record("ACTION_REJECTED", null, null, saved.getId());
        return saved;
    }

    public List<SuspensionResponse> getSuspensions(Long actionId) {
        proposedActionRepository.findById(actionId).orElseThrow(() -> new NotFoundException("Action not found"));
        return suspensionRepository.findByProposedAction_Id(actionId).stream()
                .map(s -> new SuspensionResponse(
                        s.getId(),
                        s.getReason(),
                        s.getStartDate(),
                        s.getEndDate(),
                        s.getDurationMinutes(),
                        s.getEndDate() == null ? "ongoing" : String.valueOf(s.getDurationMinutes())))
                .toList();
    }

    private ProposedAction transition(Long id, String event, String suspendReason) {
        ProposedAction action = proposedActionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Action not found"));
        ActionState state = states.get(action.getStatus());
        ActionStatus next = switch (event.toLowerCase()) {
            case "implement" -> state.implement();
            case "suspend" -> state.suspend();
            case "resume" -> state.resume(action);
            case "complete" -> state.complete();
            case "abandon" -> state.abandon(action);
            default -> throw new IllegalArgumentException("Unknown event: " + event);
        };
        action.setStatus(next);
        ProposedAction saved = proposedActionRepository.save(action);

        if (next == ActionStatus.SUSPENDED) {
            Suspension suspension = new Suspension();
            suspension.setProposedAction(saved);
            suspension.setReason(suspendReason);
            suspension.setStartDate(clock.instant());
            suspensionRepository.save(suspension);
        }

        if ("implement".equalsIgnoreCase(event)) {
            ImplementedAction implementedAction = new ImplementedAction();
            implementedAction.setProposedAction(saved);
            implementedAction.setActualStart(clock.instant());
            implementedAction.setActualParty(saved.getParty());
            implementedAction.setActualLocation(saved.getLocation());
            implementedActionRepository.save(implementedAction);
        }

        if ("complete".equalsIgnoreCase(event)) {
            implementedActionRepository.findByProposedAction_Id(saved.getId()).ifPresent(ledgerEntryEngine::generate);
        }

        String auditEvent = switch (event.toLowerCase()) {
            case "implement" -> "ACTION_IMPLEMENTED";
            case "suspend" -> "ACTION_SUSPENDED";
            case "resume" -> "ACTION_RESUMED";
            case "complete" -> "ACTION_COMPLETED";
            case "abandon" -> "ACTION_ABANDONED";
            default -> "ACTION_TRANSITION";
        };
        auditLogManager.record(auditEvent, null, null, saved.getId());

        return saved;
    }
}
