package com.rpl.domain.state;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.Suspension;
import com.rpl.exception.IllegalStateTransitionException;
import com.rpl.resourceaccess.SuspensionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewingStateTest {

    private static final Instant T0 = Instant.parse("2026-06-01T12:00:00Z");

    @Mock
    private SuspensionRepository suspensionRepository;

    private ReviewingState reviewingState;

    @BeforeEach
    void setUp() {
        reviewingState = new ReviewingState(suspensionRepository, Clock.fixed(T0, ZoneOffset.UTC));
    }

    @Test
    void approve_returnsCompleted() {
        Assertions.assertEquals(ActionStatus.COMPLETED, reviewingState.approve());
    }

    @Test
    void reject_returnsInProgressAndCreatesInstantSuspension() {
        // Arrange
        ProposedAction pa = new ProposedAction();
        pa.setId(77L);
        when(suspensionRepository.save(any(Suspension.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Assertions.assertEquals(ActionStatus.IN_PROGRESS, reviewingState.reject(pa, "scope creep"));

        // Assert
        ArgumentCaptor<Suspension> cap = ArgumentCaptor.forClass(Suspension.class);
        verify(suspensionRepository).save(cap.capture());
        Suspension s = cap.getValue();
        Assertions.assertEquals(T0, s.getStartDate());
        Assertions.assertEquals(T0, s.getEndDate());
        Assertions.assertEquals("scope creep", s.getReason());
        Assertions.assertEquals(pa, s.getProposedAction());
    }

    @Test
    void suspend_throwsIllegalTransition() {
        Assertions.assertThrows(IllegalStateTransitionException.class, reviewingState::suspend);
    }
}
