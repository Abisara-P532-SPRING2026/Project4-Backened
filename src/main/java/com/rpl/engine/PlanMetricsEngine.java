package com.rpl.engine;

import com.rpl.domain.Plan;
import com.rpl.domain.composite.PlanNodeVisitor;
import org.springframework.stereotype.Service;

@Service
public class PlanMetricsEngine {

    public <V extends PlanNodeVisitor> V run(Plan plan, V visitor) {
        DepthFirstPlanIterator it = new DepthFirstPlanIterator(plan);
        while (it.hasNext()) {
            it.next().accept(visitor);
        }
        return visitor;
    }
}
