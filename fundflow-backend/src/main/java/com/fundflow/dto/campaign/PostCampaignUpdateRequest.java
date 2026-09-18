package com.fundflow.dto.campaign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// For an organizer posting a progress update/story to an already-approved campaign.
@Data
public class PostCampaignUpdateRequest {

    @NotBlank
    @Size(max = 2000)
    private String updateText;
}
