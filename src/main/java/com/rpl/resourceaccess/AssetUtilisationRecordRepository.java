package com.rpl.resourceaccess;

import com.rpl.domain.AssetUtilisationRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetUtilisationRecordRepository extends JpaRepository<AssetUtilisationRecord, Long> {
    List<AssetUtilisationRecord> findByImplementedAction_Id(Long implementedActionId);

    List<AssetUtilisationRecord> findByAssetIdOrderByEndTimeDesc(String assetId);
}
