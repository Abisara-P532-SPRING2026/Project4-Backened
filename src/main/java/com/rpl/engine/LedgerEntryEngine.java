package com.rpl.engine;

import com.rpl.domain.ImplementedAction;
import org.springframework.stereotype.Service;

@Service
public class LedgerEntryEngine {
    private final ConsumableLedgerEntryGenerator consumableLedgerEntryGenerator;
    private final AssetLedgerEntryGenerator assetLedgerEntryGenerator;

    public LedgerEntryEngine(
            ConsumableLedgerEntryGenerator consumableLedgerEntryGenerator,
            AssetLedgerEntryGenerator assetLedgerEntryGenerator) {
        this.consumableLedgerEntryGenerator = consumableLedgerEntryGenerator;
        this.assetLedgerEntryGenerator = assetLedgerEntryGenerator;
    }

    public void generate(ImplementedAction action) {
        consumableLedgerEntryGenerator.generateEntries(action);
        assetLedgerEntryGenerator.generateEntries(action);
    }
}
