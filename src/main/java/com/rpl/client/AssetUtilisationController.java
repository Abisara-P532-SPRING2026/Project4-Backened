package com.rpl.client;

import com.rpl.client.dto.AssetUtilisationResponse;
import com.rpl.manager.AssetUtilisationQueryManager;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asset-utilisations")
public class AssetUtilisationController {
    private final AssetUtilisationQueryManager assetUtilisationQueryManager;

    public AssetUtilisationController(AssetUtilisationQueryManager assetUtilisationQueryManager) {
        this.assetUtilisationQueryManager = assetUtilisationQueryManager;
    }

    @GetMapping
    public List<AssetUtilisationResponse> byAsset(@RequestParam String assetId) {
        return assetUtilisationQueryManager.listByAssetId(assetId);
    }
}
