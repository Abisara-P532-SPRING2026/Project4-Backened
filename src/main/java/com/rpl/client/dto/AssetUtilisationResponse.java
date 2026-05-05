package com.rpl.client.dto;

import java.time.Instant;

public record AssetUtilisationResponse(
        String assetId,
        String resourceTypeName,
        Instant startTime,
        Instant endTime,
        Long durationMinutes) {
}
