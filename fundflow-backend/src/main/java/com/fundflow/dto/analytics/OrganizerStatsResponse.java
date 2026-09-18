package com.fundflow.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class OrganizerStatsResponse {
    private BigDecimal totalRaised;
    private long totalDonors;
    private long donationCount;
    private long campaignCount;
    private long approvedCampaignCount;
    private long pendingCampaignCount;
}
