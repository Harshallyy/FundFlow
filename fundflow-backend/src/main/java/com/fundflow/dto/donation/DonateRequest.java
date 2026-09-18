package com.fundflow.dto.donation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DonateRequest {

    @NotNull
    private Long campaignId;

    @NotNull
    @Positive(message = "Donation amount must be greater than zero")
    private BigDecimal amount;

    // e.g. "MOCK_CARD", "MOCK_UPI" - free text now, becomes an enum if/when
    // a real gateway is added with a fixed set of methods.
    @NotBlank
    private String paymentMethod;
}
