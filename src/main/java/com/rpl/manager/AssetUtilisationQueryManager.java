package com.rpl.manager;

import com.rpl.client.dto.AssetUtilisationResponse;
import com.rpl.domain.AssetUtilisationRecord;
import com.rpl.exception.NotFoundException;
import com.rpl.exception.ValidationException;
import com.rpl.resourceaccess.AssetUtilisationRecordRepository;
import com.rpl.resourceaccess.ImplementedActionRepository;
import com.rpl.resourceaccess.ProposedActionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AssetUtilisationQueryManager {
    private final AssetUtilisationRecordRepository assetUtilisationRecordRepository;
    private final ImplementedActionRepository implementedActionRepository;
    private final ProposedActionRepository proposedActionRepository;

    public AssetUtilisationQueryManager(
            AssetUtilisationRecordRepository assetUtilisationRecordRepository,
            ImplementedActionRepository implementedActionRepository,
            ProposedActionRepository proposedActionRepository) {
        this.assetUtilisationRecordRepository = assetUtilisationRecordRepository;
        this.implementedActionRepository = implementedActionRepository;
        this.proposedActionRepository = proposedActionRepository;
    }

    public List<AssetUtilisationResponse> listForProposedAction(Long proposedActionId) {
        proposedActionRepository.findById(proposedActionId).orElseThrow(() -> new NotFoundException("Action not found"));
        return implementedActionRepository.findByProposedAction_Id(proposedActionId)
                .map(ia -> assetUtilisationRecordRepository.findByImplementedAction_Id(ia.getId()).stream()
                        .map(this::toResponse)
                        .toList())
                .orElse(List.of());
    }

    public List<AssetUtilisationResponse> listByAssetId(String assetId) {
        if (assetId == null || assetId.isBlank()) {
            throw new ValidationException("assetId query parameter is required");
        }
        return assetUtilisationRecordRepository.findByAssetIdOrderByEndTimeDesc(assetId).stream()
                .map(this::toResponse)
                .toList();
    }

    private AssetUtilisationResponse toResponse(AssetUtilisationRecord r) {
        String typeName =
                r.getResourceAllocation().getResourceType() != null
                        ? r.getResourceAllocation().getResourceType().getName()
                        : "";
        return new AssetUtilisationResponse(
                r.getAssetId(), typeName, r.getStartTime(), r.getEndTime(), r.getDurationMinutes());
    }
}
