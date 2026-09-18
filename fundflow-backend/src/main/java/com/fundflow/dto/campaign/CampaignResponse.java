package com.fundflow.dto.campaign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Full shape for the campaign details page.
@Data
@Builder
@AllArgsConstructor
public class CampaignResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private int progressPercentage;
    private int donorCount;
    private int daysRemaining;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long organizerId;
    private String organizerName;
    private String beneficiaryName;
    private String beneficiaryInfo;
    private String imagePath;
    private String status;
    private String reviewNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
