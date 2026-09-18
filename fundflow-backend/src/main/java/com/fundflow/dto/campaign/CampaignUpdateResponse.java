package com.fundflow.dto.campaign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CampaignUpdateResponse {
    private Long id;
    private Long campaignId;
    private String updateText;
    private LocalDateTime createdAt;
}
