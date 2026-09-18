package com.fundflow.service;

import com.fundflow.dto.campaign.*;
import com.fundflow.entity.User;

import java.util.List;

public interface CampaignService {

    CampaignResponse createDraft(User organizer, CampaignCreateRequest request);

    CampaignResponse updateDraft(User organizer, Long campaignId, CampaignEditRequest request);

    CampaignResponse submitForReview(User organizer, Long campaignId);

    CampaignResponse reviewCampaign(Long campaignId, CampaignReviewRequest request);

    /** Admin marks an APPROVED campaign as COMPLETED (goal reached, or fundraising period wrapped up). */
    CampaignResponse completeCampaign(Long campaignId);

    /**
     * Fetches full campaign details. If the campaign is not APPROVED, only the
     * owning organizer or an admin may view it - this endpoint is public, so
     * anonymous/other users get a 404 for non-approved campaigns rather than
     * a 403, to avoid revealing that a hidden campaign exists.
     * @param requester the authenticated caller, or null if anonymous
     */
    CampaignResponse getCampaignDetails(Long campaignId, User requester);

    List<CampaignSummaryResponse> browseApprovedCampaigns(String category, String keyword);

    List<CampaignSummaryResponse> listByOrganizer(Long organizerId);

    List<CampaignSummaryResponse> listPendingForAdmin();

    List<CampaignSummaryResponse> listAllForAdmin(String status);
}
