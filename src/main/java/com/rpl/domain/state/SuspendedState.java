package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ProposedAction;
import com.rpl.exception.IllegalStateTransitionException;
import com.rpl.resourceaccess.SuspensionRepository;
import java.time.Clock;
import org.springframework.stereotype.Component;

@Component
public class SuspendedState implements ActionState {
    private final SuspensionRepository suspensionRepository;
    private final Clock clock;

    public SuspendedState(SuspensionRepository suspensionRepository, Clock clock) {
        this.suspensionRepository = suspensionRepository;
        this.clock = clock;
    }

    @Override
    public String name() {
        return "SUSPENDED";
    }

    @Override
    public ActionStatus implement() {
        throw new IllegalStateTransitionException("Cannot implement from SUSPENDED");
    }

    @Override
    public ActionStatus suspend() {
        throw new IllegalStateTransitionException("Cannot suspend from SUSPENDED");
    }

    @Override
    public ActionStatus resume(ProposedAction action) {
        suspensionRepository.findOpenByActionId(action.getId()).ifPresent(s -> {
            s.setEndDate(clock.instant());
            suspensionRepository.save(s);
        });
        return ActionStatus.PROPOSED;
    }

    @Override
    public ActionStatus complete() {
        throw new IllegalStateTransitionException("Cannot complete from SUSPENDED");
    }

    @Override
    public ActionStatus abandon(ProposedAction action) {
        suspensionRepository.findOpenByActionId(action.getId()).ifPresent(s -> {
            s.setEndDate(clock.instant());
            suspensionRepository.save(s);
        });
        return ActionStatus.ABANDONED;
    }

    @Override
    public ActionStatus review(ProposedAction action) {
        throw new IllegalStateTransitionException("Cannot review from SUSPENDED");
    }

    @Override
    public ActionStatus approve() {
        throw new IllegalStateTransitionException("Cannot approve from SUSPENDED");
    }

    @Override
    public ActionStatus reject(ProposedAction action, String reason) {
        throw new IllegalStateTransitionException("Cannot reject from SUSPENDED");
    }
}
