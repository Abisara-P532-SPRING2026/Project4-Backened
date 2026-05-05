package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ProposedAction;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class CompletedState implements ActionState {
    @Override
    public String name() {
        return "COMPLETED";
    }

    @Override
    public ActionStatus implement() {
        throw new IllegalStateTransitionException("Cannot implement from COMPLETED");
    }

    @Override
    public ActionStatus suspend() {
        throw new IllegalStateTransitionException("Cannot suspend from COMPLETED");
    }

    @Override
    public ActionStatus resume(ProposedAction action) {
        throw new IllegalStateTransitionException("Cannot resume from COMPLETED");
    }

    @Override
    public ActionStatus complete() {
        throw new IllegalStateTransitionException("Cannot complete from COMPLETED");
    }

    @Override
    public ActionStatus abandon(ProposedAction action) {
        throw new IllegalStateTransitionException("Cannot abandon from COMPLETED");
    }

    @Override
    public ActionStatus review(ProposedAction action) {
        throw new IllegalStateTransitionException("Cannot review from COMPLETED");
    }

    @Override
    public ActionStatus approve() {
        throw new IllegalStateTransitionException("Cannot approve from COMPLETED");
    }

    @Override
    public ActionStatus reject(ProposedAction action, String reason) {
        throw new IllegalStateTransitionException("Cannot reject from COMPLETED");
    }
}
