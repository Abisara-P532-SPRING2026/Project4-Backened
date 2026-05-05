package com.rpl.engine;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.domain.composite.PlanNodeVisitor;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ResourceUtilisationVisitor implements PlanNodeVisitor {
    private final Map<String, BigDecimal> totals = new HashMap<>();

    @Override
    public void visit(ProposedAction leaf) {
        for (ResourceAllocation ra : leaf.getAllocations()) {
            if (ra.getResourceType() == null) {
                continue;
            }
            String name = ra.getResourceType().getName();
            totals.merge(name, BigDecimal.valueOf(ra.getQuantity()), BigDecimal::add);
        }
    }

    @Override
    public void visit(Plan composite) {
        // no-op
    }

    public Map<String, BigDecimal> getTotals() {
        return totals;
    }
}
