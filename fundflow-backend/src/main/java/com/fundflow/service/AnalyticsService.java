package com.fundflow.service;

import com.fundflow.dto.analytics.AdminStatsResponse;
import com.fundflow.dto.analytics.DonorStatsResponse;
import com.fundflow.dto.analytics.OrganizerStatsResponse;

public interface AnalyticsService {

    OrganizerStatsResponse getOrganizerStats(Long organizerId);

    AdminStatsResponse getAdminStats();

    DonorStatsResponse getDonorStats(Long donorId);
}
