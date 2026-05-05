package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.Suspension;
import com.rpl.exception.IllegalStateTransitionException;
import com.rpl.resourceaccess.SuspensionRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class ReviewingState implements ActionState {
    private final SuspensionRepository suspensionRepository;
    private final Clock clock;

    public ReviewingState(SuspensionRepository suspensionRepository, Clock clock) {
        this.suspensionRepository = suspensionRepository;
        this.clock = clock;
    }

    @Override
    public String name() {
        return "REVIEWING";
    }

    @Override
    public ActionStatus implement() {
        throw new IllegalStateTransitionException("Cannot implement from REVIEWING");
    }

    @Override
    public ActionStatus suspend() {
        throw new IllegalStateTransitionException("Cannot suspend from REVIEWING");
    }

    @Override
    public ActionStatus resume(ProposedAction action) {
        throw new IllegalStateTransitionException("Cannot resume from REVIEWING");
    }

    @Override
    public ActionStatus complete() {
        throw new IllegalStateTransitionException("Cannot complete from REVIEWING — use approve");
    }

    @Override
    public ActionStatus abandon(ProposedAction action) {
        return ActionStatus.ABANDONED;
    }

    @Override
    public ActionStatus review(ProposedAction action) {
        throw new IllegalStateTransitionException("Cannot review from REVIEWING");
    }

    @Override
    public ActionStatus approve() {
        return ActionStatus.COMPLETED;
    }

    @Override
    public ActionStatus reject(ProposedAction action, String reason) {
        Suspension s = new Suspension();
        s.setProposedAction(action);
        s.setReason(reason);
        Instant now = clock.instant();
        s.setStartDate(now);
        s.setEndDate(now);
        suspensionRepository.save(s);
        return ActionStatus.IN_PROGRESS;
    }
}
