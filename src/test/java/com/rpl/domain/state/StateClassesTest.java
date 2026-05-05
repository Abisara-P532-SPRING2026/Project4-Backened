package com.rpl.domain.state;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ProposedAction;
import com.rpl.exception.IllegalStateTransitionException;
import com.rpl.resourceaccess.SuspensionRepository;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StateClassesTest {
    @Test
    void proposedImplementTransitionsToInProgress() {
        ProposedState proposedState = new ProposedState();
        Assertions.assertEquals(ActionStatus.IN_PROGRESS, proposedState.implement());
    }

    @Test
    void suspendedResumeTransitionsToProposed() {
        SuspensionRepository repo = mock(SuspensionRepository.class);
        when(repo.findOpenByActionId(9L)).thenReturn(Optional.empty());
        SuspendedState suspendedState = new SuspendedState(repo, Clock.systemUTC());
        ProposedAction pa = new ProposedAction();
        pa.setId(9L);
        Assertions.assertEquals(ActionStatus.PROPOSED, suspendedState.resume(pa));
    }

    @Test
    void inProgressCompleteTransitionsToCompleted() {
        InProgressState inProgressState = new InProgressState();
        Assertions.assertEquals(ActionStatus.COMPLETED, inProgressState.complete());
    }

    @Test
    void inProgressReviewTransitionsToReviewing() {
        InProgressState inProgressState = new InProgressState();
        ProposedAction pa = new ProposedAction();
        pa.setId(1L);
        Assertions.assertEquals(ActionStatus.REVIEWING, inProgressState.review(pa));
    }

    @Test
    void proposedCompleteThrows() {
        ProposedState proposedState = new ProposedState();
        Assertions.assertThrows(IllegalStateTransitionException.class, proposedState::complete);
    }
}
