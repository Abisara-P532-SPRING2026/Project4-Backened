package com.rpl.engine;

import com.rpl.domain.AssetUtilisationRecord;
import com.rpl.domain.ImplementedAction;
import com.rpl.domain.LedgerTransaction;
import com.rpl.domain.ResourceAllocation;
import com.rpl.domain.ResourceKind;
import com.rpl.exception.ValidationException;
import com.rpl.manager.AuditLogManager;
import com.rpl.resourceaccess.AccountRepository;
import com.rpl.resourceaccess.AssetUtilisationRecordRepository;
import com.rpl.resourceaccess.EntryRepository;
import com.rpl.resourceaccess.ImplementedActionRepository;
import com.rpl.resourceaccess.LedgerTransactionRepository;
import com.rpl.resourceaccess.ResourceAllocationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AssetLedgerEntryGenerator extends AbstractLedgerEntryGenerator {
    private static final Logger log = LoggerFactory.getLogger(AssetLedgerEntryGenerator.class);
    private static final Pattern IA_DESC = Pattern.compile("Complete action #(\\d+)");

    private final ResourceAllocationRepository allocationRepository;
    private final AssetUtilisationRecordRepository assetUtilisationRecordRepository;
    private final ImplementedActionRepository implementedActionRepository;
    private final Clock clock;

    public AssetLedgerEntryGenerator(
            LedgerTransactionRepository transactionRepository,
            EntryRepository entryRepository,
            AccountRepository accountRepository,
            PostingRuleEngine postingRuleEngine,
            AuditLogManager auditLogManager,
            ResourceAllocationRepository allocationRepository,
            AssetUtilisationRecordRepository assetUtilisationRecordRepository,
            ImplementedActionRepository implementedActionRepository,
            Clock clock) {
        super(transactionRepository, entryRepository, accountRepository, postingRuleEngine, auditLogManager);
        this.allocationRepository = allocationRepository;
        this.assetUtilisationRecordRepository = assetUtilisationRecordRepository;
        this.implementedActionRepository = implementedActionRepository;
        this.clock = clock;
    }

    @Override
    protected List<ResourceAllocation> selectAllocations(ImplementedAction action) {
        return allocationRepository.findByActionId(action.getProposedAction().getId()).stream()
                .filter(a -> a.getResourceType().getKind() == ResourceKind.ASSET)
                .toList();
    }

    @Override
    protected void validate(List<ResourceAllocation> allocations) {
        for (ResourceAllocation allocation : allocations) {
            if (allocation.getKind() == com.rpl.domain.AllocationKind.SPECIFIC
                    && (allocation.getAssetId() == null || allocation.getAssetId().isBlank())) {
                throw new ValidationException("SPECIFIC ASSET allocation requires assetId");
            }
            if (allocation.getQuantity() <= 0) {
                throw new ValidationException("Quantity must be positive");
            }
        }
    }

    @Override
    protected void afterPost(LedgerTransaction tx) {
        Long iaId = parseImplementedActionId(tx.getDescription());
        if (iaId == null) {
            return;
        }
        implementedActionRepository.findById(iaId).ifPresent(ia -> {
            Instant end = clock.instant();
            List<ResourceAllocation> assets = selectAllocations(ia);
            for (ResourceAllocation allocation : assets) {
                try {
                    AssetUtilisationRecord row = new AssetUtilisationRecord();
                    row.setImplementedAction(ia);
                    row.setResourceAllocation(allocation);
                    row.setAssetId(allocation.getAssetId());
                    Instant start = ia.getActualStart();
                    row.setStartTime(start);
                    row.setEndTime(end);
                    row.setDurationMinutes(
                            start != null ? ChronoUnit.MINUTES.between(start, end) : 0L);
                    assetUtilisationRecordRepository.save(row);
                } catch (Exception e) {
                    log.warn("Asset utilisation record save failed: {}", e.getMessage(), e);
                }
            }
        });
    }

    private static Long parseImplementedActionId(String description) {
        if (description == null) {
            return null;
        }
        Matcher m = IA_DESC.matcher(description);
        return m.find() ? Long.parseLong(m.group(1)) : null;
    }
}
