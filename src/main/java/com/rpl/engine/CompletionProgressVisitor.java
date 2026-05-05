package com.rpl.engine;

import com.rpl.client.dto.PlanMetricsProgressResponse;
import com.rpl.domain.ActionStatus;
import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.PlanNodeVisitor;

/** Counts non-abandoned leaves; completed excludes abandoned from numerator logic via total rules. */
public class CompletionProgressVisitor implements PlanNodeVisitor {
    private long total;
    private long completed;

    @Override
    public void visit(ProposedAction leaf) {
        if (leaf.getStatus() == ActionStatus.ABANDONED) {
            return;
        }
        total++;
        if (leaf.getStatus() == ActionStatus.COMPLETED) {
            completed++;
        }
    }

    @Override
    public void visit(Plan composite) {
        // Iterator drives traversal — no recursion here.
    }

    public PlanMetricsProgressResponse result() {
        double pct = total == 0 ? 0.0 : (100.0 * completed / total);
        return new PlanMetricsProgressResponse(total, completed, pct);
    }
}
