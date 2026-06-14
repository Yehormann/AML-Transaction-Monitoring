package com.aml.dto;

public record DashboardStatsResponse(
        long totalTransactions,
        long flaggedTransactions,
        double flaggedPercentage,
        long openAlerts,
        long dismissedAlerts,
        long escalatedAlerts,
        long totalSarsFiled
) {}
