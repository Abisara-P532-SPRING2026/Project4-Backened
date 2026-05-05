package com.rpl.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rpl.resourceaccess.SuspensionRepository;
import org.junit.jupiter.api.Test;

class ProposedActionTotalSuspendedMinutesTest {

    @Test
    void getTotalSuspendedMinutes_nullId_returnsZero() {
        ProposedAction pa = new ProposedAction();
        SuspensionRepository repo = mock(SuspensionRepository.class);
        assertEquals(0L, pa.getTotalSuspendedMinutes(repo));
    }

    @Test
    void getTotalSuspendedMinutes_delegatesToRepository() {
        ProposedAction pa = new ProposedAction();
        pa.setId(9L);
        SuspensionRepository repo = mock(SuspensionRepository.class);
        when(repo.sumClosedSuspendedMinutesByProposedActionId(9L)).thenReturn(120L);
        assertEquals(120L, pa.getTotalSuspendedMinutes(repo));
    }
}
