package com.fundflow.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AssistantResponse {
    private String reply;
}
