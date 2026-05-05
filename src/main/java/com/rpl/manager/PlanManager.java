package com.rpl.manager;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.Protocol;
import com.rpl.domain.ProtocolStep;
import com.rpl.engine.CompletionProgressVisitor;
import com.rpl.engine.OverdueActionVisitor;
import com.rpl.engine.PlanMetricsEngine;
import com.rpl.engine.ResourceUtilisationVisitor;
import com.rpl.exception.NotFoundException;
import com.rpl.exception.ValidationException;
import com.rpl.resourceaccess.PlanRepository;
import com.rpl.resourceaccess.ProtocolRepository;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlanManager {
    private final PlanRepository planRepository;
    private final ProtocolRepository protocolRepository;
    private final PlanMetricsEngine planMetricsEngine;
    private final Clock clock;

    public PlanManager(
            PlanRepository planRepository,
            ProtocolRepository protocolRepository,
            PlanMetricsEngine planMetricsEngine,
            Clock clock) {
        this.planRepository = planRepository;
        this.protocolRepository = protocolRepository;
        this.planMetricsEngine = planMetricsEngine;
        this.clock = clock;
    }

    public Plan create(String name, Long protocolId, Long parentPlanId) {
        Plan plan = new Plan();
        plan.setName(name);
        if (protocolId != null) {
            Protocol protocol = protocolRepository.findById(protocolId)
                    .orElseThrow(() -> new NotFoundException("Protocol not found"));
            List<ProtocolStep> steps = protocol.getSteps();
            if (steps.isEmpty()) {
                throw new ValidationException(
                    "Protocol '" + protocol.getName() + "' has no steps. " +
                    "Add steps via POST /api/protocols/" + protocolId + "/steps before creating a plan.");
            }
            for (ProtocolStep step : steps) {
                ProposedAction action = new ProposedAction();
                action.setName(step.getName());
                plan.addLeaf(action);
            }
        }
        if (parentPlanId != null) {
            Plan parent = planRepository.findById(parentPlanId)
                    .orElseThrow(() -> new NotFoundException("Parent plan not found"));
            parent.addSubPlan(plan);
            planRepository.save(parent);
            return plan;
        }
        return planRepository.save(plan);
    }

    public Plan get(Long id) {
        return planRepository.findById(id).orElseThrow(() -> new NotFoundException("Plan not found"));
    }

    public List<Plan> list() {
        return planRepository.findAll();
    }

    public Object getMetrics(Long planId, String type) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new NotFoundException("Plan not found"));
        if (type == null || type.isBlank()) {
            throw new ValidationException("Unknown metric type: ");
        }
        return switch (type.toLowerCase()) {
            case "progress" -> planMetricsEngine.run(plan, new CompletionProgressVisitor()).result();
            case "utilisation" -> planMetricsEngine.run(plan, new ResourceUtilisationVisitor()).getTotals();
            case "overdue" -> planMetricsEngine.run(plan, new OverdueActionVisitor(clock)).result();
            default -> throw new ValidationException("Unknown metric type: " + type);
        };
    }
}
