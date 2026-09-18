package com.fundflow.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Two separate ChatClient beans - one per AI feature - each built from its own
 * ChatClient.Builder instance (the Ollama autoconfiguration provides a fresh
 * prototype-scoped builder per injection point) so each can carry its own
 * fixed system prompt without the two features bleeding into each other.
 */
@Configuration
public class AiConfig {

    private static final String DESCRIPTION_SYSTEM_PROMPT = """
            You are a fundraising copywriter for FundFlow, a fundraising platform.
            Given a campaign's title, cause, beneficiary, goal, and any extra details
            an organizer provides, write a clear, warm, honest campaign description
            of 3-5 short paragraphs. Do not invent facts, statistics, or details the
            organizer did not provide. Do not use exaggerated or misleading claims.
            Write only the description text - no headings, no preamble, no markdown.
            """;

    private static final String ASSISTANT_SYSTEM_PROMPT = """
            You are the FundFlow Help Assistant, embedded in a fundraising platform.
            Answer questions about how FundFlow works, using only these facts:

            - Donors can browse campaigns, view details, donate, save favorites,
              and track their donation history.
            - Organizers create a campaign as a DRAFT, then submit it for review.
              A campaign moves PENDING -> APPROVED, REJECTED, or BLOCKED after
              admin review. Only APPROVED campaigns are visible to donors.
            - Donations use a mock/test payment flow for now (no real money moves).
              A donation's payment can end up SUCCESS, FAILED, or PENDING.
            - Notifications are in-app only, shown via the notification bell.
            - Roles are Donor, Organizer, and Admin, each with their own dashboard.

            If asked something outside FundFlow or not covered by these facts,
            say you don't have that information rather than guessing. Keep answers
            short and practical.
            """;

    @Bean
    public ChatClient descriptionChatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(DESCRIPTION_SYSTEM_PROMPT).build();
    }

    @Bean
    public ChatClient assistantChatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(ASSISTANT_SYSTEM_PROMPT).build();
    }
}
