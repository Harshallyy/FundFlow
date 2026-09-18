package com.fundflow.dto.campaign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CampaignReviewRequest {

    @NotBlank
    @Pattern(regexp = "APPROVE|REJECT|BLOCK", message = "Action must be APPROVE, REJECT or BLOCK")
    private String action;

    // Required by the service layer for REJECT/BLOCK; optional for APPROVE.
    private String reviewNotes;
}
