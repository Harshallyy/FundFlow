package com.fundflow.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateDescriptionRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String cause; // e.g. "medical treatment", "education", "disaster relief"

    @NotBlank
    private String beneficiary;

    @NotBlank
    private String goal; // free text, e.g. "raise $5,000 for surgery costs"

    private String importantDetails; // optional extra context the organizer wants included
}
