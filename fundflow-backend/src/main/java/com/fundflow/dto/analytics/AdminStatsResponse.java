package com.fundflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalCampaigns;
    private long approvedCampaigns;
    private long pendingCampaigns;
    private long rejectedCampaigns;
    private long blockedCampaigns;
    private long completedCampaigns;
    private long totalSuccessfulDonations;
    private BigDecimal totalTransactionAmount;
}
