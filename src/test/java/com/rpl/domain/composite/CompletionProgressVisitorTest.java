package com.rpl.domain.composite;

import com.rpl.client.dto.PlanMetricsProgressResponse;
import com.rpl.domain.ActionStatus;
import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.engine.CompletionProgressVisitor;
import com.rpl.engine.DepthFirstPlanIterator;
import com.rpl.engine.PlanMetricsEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CompletionProgressVisitorTest {

    @Test
    void completionProgressVisitor_twoLeavesOneCompleted_pctIs50() {
        Plan root = new Plan();
        ProposedAction done = new ProposedAction();
        done.setStatus(ActionStatus.COMPLETED);
        ProposedAction open = new ProposedAction();
        open.setStatus(ActionStatus.IN_PROGRESS);
        root.addLeaf(done);
        root.addLeaf(open);

        CompletionProgressVisitor v = new PlanMetricsEngine().run(root, new CompletionProgressVisitor());
        PlanMetricsProgressResponse r = v.result();
        Assertions.assertEquals(2, r.total());
        Assertions.assertEquals(1, r.completed());
        Assertions.assertEquals(50.0, r.percentComplete(), 0.001);
    }

    @Test
    void completionProgressVisitor_abandonedNotCountedInTotal() {
        Plan root = new Plan();
        ProposedAction abandoned = new ProposedAction();
        abandoned.setStatus(ActionStatus.ABANDONED);
        ProposedAction done = new ProposedAction();
        done.setStatus(ActionStatus.COMPLETED);
        root.addLeaf(abandoned);
        root.addLeaf(done);

        CompletionProgressVisitor v = new PlanMetricsEngine().run(root, new CompletionProgressVisitor());
        PlanMetricsProgressResponse r = v.result();
        Assertions.assertEquals(1, r.total());
        Assertions.assertEquals(1, r.completed());
    }

    @Test
    void depthFirstPlanIterator_visitsCompositeThenLeaves_depthFirst() {
        Plan root = new Plan();
        ProposedAction a = new ProposedAction();
        a.setName("A");
        ProposedAction b = new ProposedAction();
        b.setName("B");
        root.addLeaf(a);
        root.addLeaf(b);
        DepthFirstPlanIterator direct = new DepthFirstPlanIterator(root);
        int directSteps = 0;
        while (direct.hasNext()) {
            direct.next();
            directSteps++;
        }
        Assertions.assertEquals(3, directSteps);
        CompletionProgressVisitor viaEngine = new PlanMetricsEngine().run(root, new CompletionProgressVisitor());
        Assertions.assertNotNull(viaEngine.result());
    }
}
