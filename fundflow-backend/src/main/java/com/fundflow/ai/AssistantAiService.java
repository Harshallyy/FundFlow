package com.fundflow.ai;

public interface AssistantAiService {

    /** Answers a user's question about how FundFlow works, grounded in a fixed system prompt. */
    String ask(String message);
}
