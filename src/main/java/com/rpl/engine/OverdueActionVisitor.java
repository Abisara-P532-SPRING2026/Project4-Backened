package com.rpl.engine;

import com.rpl.client.dto.PlanMetricsOverdueResponse;
import com.rpl.domain.ActionStatus;
import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.PlanNodeVisitor;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class OverdueActionVisitor implements PlanNodeVisitor {
    private final Clock clock;
    private final List<PlanMetricsOverdueResponse.OverdueActionRef> overdue = new ArrayList<>();

    public OverdueActionVisitor(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void visit(ProposedAction leaf) {
        if (leaf.getTimeRef() == null) {
            return;
        }
        if (leaf.getStatus() != ActionStatus.IN_PROGRESS && leaf.getStatus() != ActionStatus.REVIEWING) {
            return;
        }
        if (leaf.getTimeRef().isBefore(clock.instant())) {
            overdue.add(new PlanMetricsOverdueResponse.OverdueActionRef(leaf.getId(), leaf.getName()));
        }
    }

    @Override
    public void visit(Plan composite) {
        // no-op
    }

    public PlanMetricsOverdueResponse result() {
        return new PlanMetricsOverdueResponse(overdue);
    }
}
