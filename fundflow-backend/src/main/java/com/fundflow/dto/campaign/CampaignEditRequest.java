package com.fundflow.dto.campaign;

// Same shape as create - kept as a separate class (rather than reusing
// CampaignCreateRequest) since edit rules/validation may diverge later
// (e.g. target amount becoming non-editable after donations exist).
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CampaignEditRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String description;

    private String category;

    @NotNull
    @Positive
    private BigDecimal targetAmount;

    @NotNull
    private LocalDate startDate;

    @NotNull
    @Future
    private LocalDate endDate;

    @NotBlank
    private String beneficiaryName;

    private String beneficiaryInfo;

    private String imagePath;
}
