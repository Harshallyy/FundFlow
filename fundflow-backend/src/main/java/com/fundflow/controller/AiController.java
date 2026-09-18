package com.fundflow.controller;

import com.fundflow.ai.AssistantAiService;
import com.fundflow.ai.CampaignDescriptionAiService;
import com.fundflow.dto.ai.AssistantRequest;
import com.fundflow.dto.ai.AssistantResponse;
import com.fundflow.dto.ai.GenerateDescriptionRequest;
import com.fundflow.dto.ai.GenerateDescriptionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final CampaignDescriptionAiService descriptionAiService;
    private final AssistantAiService assistantAiService;

    // Organizer-only (enforced in SecurityConfig) - used from the Create/Edit
    // Campaign page. Purely generates text; never creates or publishes anything.
    @PostMapping("/generate-description")
    public ResponseEntity<GenerateDescriptionResponse> generateDescription(
            @Valid @RequestBody GenerateDescriptionRequest request) {
        String description = descriptionAiService.generateDescription(request);
        return ResponseEntity.ok(GenerateDescriptionResponse.builder()
                .generatedDescription(description)
                .build());
    }

    // Public - visitors deciding whether to sign up can use it too.
    @PostMapping("/assistant")
    public ResponseEntity<AssistantResponse> assistant(@Valid @RequestBody AssistantRequest request) {
        String reply = assistantAiService.ask(request.getMessage());
        return ResponseEntity.ok(AssistantResponse.builder().reply(reply).build());
    }
}
