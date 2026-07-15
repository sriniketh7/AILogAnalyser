package com.proj1.ailoganlzr.AILayer.Service;

import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.AILayer.Parser.AiParser;
import com.proj1.ailoganlzr.AILayer.Service.Interface.AIAnalysisService;
import com.proj1.ailoganlzr.AILayer.prompt.PromptBuilder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private final ChatClient chatClient;

    private final PromptBuilder promptBuilder;

    private final AiParser parser;

    public AIAnalysisServiceImpl(ChatClient chatClient, PromptBuilder promptBuilder, AiParser parser) {
        this.chatClient = chatClient;
        this.promptBuilder = promptBuilder;
        this.parser = parser;
    }

    @Override
    public AIAnalysisResponse analyzeStackTrace(String rawLog) {

        String prompt = promptBuilder.buildStackTracePrompt(rawLog);

        AIAnalysisResponse aiResponse = chatClient
                .prompt(prompt)
                .call()
                .entity(AIAnalysisResponse.class);

        return aiResponse;
    }
}
