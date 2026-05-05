package com.rpl.client;

import com.rpl.client.dto.AllocationRequest;
import com.rpl.client.dto.AssetUtilisationResponse;
import com.rpl.client.dto.RejectReasonRequest;
import com.rpl.client.dto.SuspendRequest;
import com.rpl.client.dto.SuspensionResponse;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.manager.ActionManager;
import com.rpl.manager.AssetUtilisationQueryManager;
import com.rpl.manager.ResourceAllocationManager;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/actions")
public class ActionController {
    private final ActionManager actionManager;
    private final ResourceAllocationManager allocationManager;
    private final AssetUtilisationQueryManager assetUtilisationQueryManager;

    public ActionController(
            ActionManager actionManager,
            ResourceAllocationManager allocationManager,
            AssetUtilisationQueryManager assetUtilisationQueryManager) {
        this.actionManager = actionManager;
        this.allocationManager = allocationManager;
        this.assetUtilisationQueryManager = assetUtilisationQueryManager;
    }

    @GetMapping("/{id}")
    public ProposedAction get(@PathVariable Long id) {
        return actionManager.get(id);
    }

    @PostMapping("/{id}/review")
    public ProposedAction review(@PathVariable Long id) {
        return actionManager.reviewAction(id);
    }

    @PostMapping("/{id}/approve")
    public ProposedAction approve(@PathVariable Long id) {
        return actionManager.approveAction(id);
    }

    @PostMapping("/{id}/reject")
    public ProposedAction reject(@PathVariable Long id, @RequestBody RejectReasonRequest body) {
        String reason = body != null && body.reason() != null ? body.reason() : "";
        return actionManager.rejectAction(id, reason);
    }

    @PostMapping("/{id}/suspend")
    public ProposedAction suspend(@PathVariable Long id, @RequestBody(required = false) SuspendRequest body) {
        String reason = body != null && body.reason() != null ? body.reason() : "";
        return actionManager.suspendWithReason(id, reason);
    }

    @GetMapping("/{id}/suspensions")
    public List<SuspensionResponse> suspensions(@PathVariable Long id) {
        return actionManager.getSuspensions(id);
    }

    @GetMapping("/{id}/utilisations")
    public List<AssetUtilisationResponse> utilisations(@PathVariable Long id) {
        return assetUtilisationQueryManager.listForProposedAction(id);
    }

    @PostMapping("/{id}/allocations")
    public ResourceAllocation allocate(@PathVariable Long id, @RequestBody AllocationRequest request) {
        ResourceAllocation allocation = new ResourceAllocation();
        allocation.setQuantity(request.quantity());
        allocation.setKind(request.kind());
        allocation.setAssetId(request.assetId());
        return allocationManager.attach(id, request.resourceTypeId(), allocation);
    }

    @PostMapping("/{id}/{event}")
    public ProposedAction transition(@PathVariable Long id, @PathVariable String event) {
        return actionManager.transition(id, event);
    }
}
