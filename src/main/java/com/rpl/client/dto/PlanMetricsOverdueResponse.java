package com.rpl.client.dto;

import java.util.List;

public record PlanMetricsOverdueResponse(List<OverdueActionRef> overdue) {
    public record OverdueActionRef(Long id, String name) {
    }
}
