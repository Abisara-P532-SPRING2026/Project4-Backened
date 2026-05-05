package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ProposedAction;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class InProgressState implements ActionState {
    @Override
    public String name() {
        return "IN_PROGRESS";
    }

    @Override
    public ActionStatus implement() {
        throw new IllegalStateTransitionException("Cannot implement from IN_PROGRESS");
    }

    @Override
    public ActionStatus suspend() {
        return ActionStatus.SUSPENDED;
    }

    @Override
    public ActionStatus resume(ProposedAction action) {
        throw new IllegalStateTransitionException("Cannot resume from IN_PROGRESS");
    }

    @Override
    public ActionStatus complete() {
        return ActionStatus.COMPLETED;
    }

    @Override
    public ActionStatus abandon(ProposedAction action) {
        return ActionStatus.ABANDONED;
    }

    @Override
    public ActionStatus review(ProposedAction action) {
        return ActionStatus.REVIEWING;
    }

    @Override
    public ActionStatus approve() {
        throw new IllegalStateTransitionException("Cannot approve from IN_PROGRESS");
    }

    @Override
    public ActionStatus reject(ProposedAction action, String reason) {
        throw new IllegalStateTransitionException("Cannot reject from IN_PROGRESS");
    }
}
