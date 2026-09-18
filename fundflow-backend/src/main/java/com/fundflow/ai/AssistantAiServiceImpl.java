package com.fundflow.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AssistantAiServiceImpl implements AssistantAiService {

    private final ChatClient chatClient;

    public AssistantAiServiceImpl(@Qualifier("assistantChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String ask(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
