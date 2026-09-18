package com.fundflow.dto.donation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class DonationResponse {
    private Long donationId;
    private Long campaignId;
    private String campaignTitle;
    private BigDecimal amount;
    private String status;        // PENDING / SUCCESS / FAILED
    private String mockReference; // from Payment
    private String transactionRef; // from TransactionLog
    private LocalDateTime createdAt;
}
