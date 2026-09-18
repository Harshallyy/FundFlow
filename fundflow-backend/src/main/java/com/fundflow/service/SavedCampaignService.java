package com.fundflow.service;

import com.fundflow.dto.campaign.CampaignSummaryResponse;
import com.fundflow.entity.User;

import java.util.List;

public interface SavedCampaignService {

    void saveCampaign(User donor, Long campaignId);

    void unsaveCampaign(User donor, Long campaignId);

    List<CampaignSummaryResponse> getSavedCampaigns(Long donorId);
}
