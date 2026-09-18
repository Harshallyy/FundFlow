package com.fundflow.dto.analytics;

import com.fundflow.dto.donation.DonationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class DonorStatsResponse {
    private BigDecimal totalDonated;
    private long donationCount;
    private List<DonationResponse> recentDonations;
}
