package com.rpl.domain.state;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ProposedAction;

public interface ActionState {
    String name();

    ActionStatus implement();

    ActionStatus suspend();

    ActionStatus resume(ProposedAction action);

    ActionStatus complete();

    ActionStatus abandon(ProposedAction action);

    /** Only legal from {@link com.rpl.domain.ActionStatus#IN_PROGRESS}. */
    ActionStatus review(ProposedAction action);

    /** Only legal from {@link com.rpl.domain.ActionStatus#REVIEWING}. */
    ActionStatus approve();

    /** Only legal from {@link com.rpl.domain.ActionStatus#REVIEWING}. */
    ActionStatus reject(ProposedAction action, String reason);
}
