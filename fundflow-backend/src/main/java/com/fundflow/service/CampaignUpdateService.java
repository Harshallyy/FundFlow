package com.fundflow.service;

import com.fundflow.dto.campaign.CampaignUpdateResponse;
import com.fundflow.dto.campaign.PostCampaignUpdateRequest;
import com.fundflow.entity.User;

import java.util.List;

public interface CampaignUpdateService {

    CampaignUpdateResponse postUpdate(User organizer, Long campaignId, PostCampaignUpdateRequest request);

    List<CampaignUpdateResponse> getUpdatesForCampaign(Long campaignId);
}
