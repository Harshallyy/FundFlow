package com.fundflow.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssistantRequest {

    @NotBlank
    private String message;
}
