package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ProposedAction;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class ProposedState implements ActionState {
    @Override
    public String name() {
        return "PROPOSED";
    }

    @Override
    public ActionStatus implement() {
        return ActionStatus.IN_PROGRESS;
    }

    @Override
    public ActionStatus suspend() {
        return ActionStatus.SUSPENDED;
    }

    @Override
    public ActionStatus resume(ProposedAction action) {
        throw new IllegalStateTransitionException("Cannot resume from PROPOSED");
    }

    @Override
    public ActionStatus complete() {
        throw new IllegalStateTransitionException("Cannot complete from PROPOSED");
    }

    @Override
    public ActionStatus abandon(ProposedAction action) {
        return ActionStatus.ABANDONED;
    }

    @Override
    public ActionStatus review(ProposedAction action) {
        throw new IllegalStateTransitionException("Cannot review from PROPOSED");
    }

    @Override
    public ActionStatus approve() {
        throw new IllegalStateTransitionException("Cannot approve from PROPOSED");
    }

    @Override
    public ActionStatus reject(ProposedAction action, String reason) {
        throw new IllegalStateTransitionException("Cannot reject from PROPOSED");
    }
}
