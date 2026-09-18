package com.fundflow.dto.campaign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

// Lightweight shape for list/browse views (Explore page, dashboards).
@Data
@Builder
@AllArgsConstructor
public class CampaignSummaryResponse {
    private Long id;
    private String title;
    private String category;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private int progressPercentage;
    private String imagePath;
    private String status;
    private int daysRemaining;
}
