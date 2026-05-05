package com.rpl.manager;

import com.rpl.client.dto.PlanReportLineResponse;
import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.PlanNode;
import com.rpl.engine.DepthFirstPlanIterator;
import com.rpl.exception.NotFoundException;
import com.rpl.resourceaccess.PlanRepository;
import com.rpl.resourceaccess.SuspensionRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportManager {
    private final PlanRepository planRepository;
    private final SuspensionRepository suspensionRepository;

    public ReportManager(PlanRepository planRepository, SuspensionRepository suspensionRepository) {
        this.planRepository = planRepository;
        this.suspensionRepository = suspensionRepository;
    }

    public List<PlanReportLineResponse> planReport(Long planId) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new NotFoundException("Plan not found"));
        List<PlanReportLineResponse> lines = new ArrayList<>();
        DepthFirstPlanIterator iterator = new DepthFirstPlanIterator(plan);
        while (iterator.hasNext()) {
            PlanNode node = iterator.next();
            String line = node.getName() + " [" + node.getStatus() + "]";
            long suspendedMinutes = 0;
            if (node instanceof ProposedAction pa) {
                suspendedMinutes = pa.getTotalSuspendedMinutes(suspensionRepository);
            }
            lines.add(new PlanReportLineResponse(line, suspendedMinutes));
        }
        return lines;
    }
}
