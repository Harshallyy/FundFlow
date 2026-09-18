package com.fundflow.ai;

import com.fundflow.dto.ai.GenerateDescriptionRequest;

public interface CampaignDescriptionAiService {

    /**
     * Generates a campaign description from organizer-provided facts.
     * Never saves or publishes anything - the organizer must still review,
     * optionally edit, and explicitly save the result onto their campaign.
     */
    String generateDescription(GenerateDescriptionRequest request);
}
