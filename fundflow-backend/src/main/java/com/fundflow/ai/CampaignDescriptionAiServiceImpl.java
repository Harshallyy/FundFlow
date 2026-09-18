package com.fundflow.ai;

import com.fundflow.dto.ai.GenerateDescriptionRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CampaignDescriptionAiServiceImpl implements CampaignDescriptionAiService {

    private final ChatClient chatClient;

    public CampaignDescriptionAiServiceImpl(@Qualifier("descriptionChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String generateDescription(GenerateDescriptionRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Campaign title: ").append(request.getTitle()).append("\n");
        prompt.append("Cause: ").append(request.getCause()).append("\n");
        prompt.append("Beneficiary: ").append(request.getBeneficiary()).append("\n");
        prompt.append("Goal: ").append(request.getGoal()).append("\n");
        if (StringUtils.hasText(request.getImportantDetails())) {
            prompt.append("Additional details from the organizer: ").append(request.getImportantDetails()).append("\n");
        }
        prompt.append("\nWrite the campaign description now.");

        return chatClient.prompt()
                .user(prompt.toString())
                .call()
                .content();
    }
}
