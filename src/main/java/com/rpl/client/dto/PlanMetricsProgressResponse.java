package com.rpl.client.dto;

public record PlanMetricsProgressResponse(long total, long completed, double percentComplete) {
}
