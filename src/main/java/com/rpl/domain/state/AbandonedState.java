package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ProposedAction;
import com.rpl.exception.IllegalStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class AbandonedState implements ActionState {
    @Override
    public String name() {
        return "ABANDONED";
    }

    @Override
    public ActionStatus implement() {
        throw new IllegalStateTransitionException("Cannot implement from ABANDONED");
    }

    @Override
    public ActionStatus suspend() {
        throw new IllegalStateTransitionException("Cannot suspend from ABANDONED");
    }

    @Override
    public ActionStatus resume(ProposedAction action) {
        throw new IllegalStateTransitionException("Cannot resume from ABANDONED");
    }

    @Override
    public ActionStatus complete() {
        throw new IllegalStateTransitionException("Cannot complete from ABANDONED");
    }

    @Override
    public ActionStatus abandon(ProposedAction action) {
        throw new IllegalStateTransitionException("Cannot abandon from ABANDONED");
    }

    @Override
    public ActionStatus review(ProposedAction action) {
        throw new IllegalStateTransitionException("Cannot review from ABANDONED");
    }

    @Override
    public ActionStatus approve() {
        throw new IllegalStateTransitionException("Cannot approve from ABANDONED");
    }

    @Override
    public ActionStatus reject(ProposedAction action, String reason) {
        throw new IllegalStateTransitionException("Cannot reject from ABANDONED");
    }
}
