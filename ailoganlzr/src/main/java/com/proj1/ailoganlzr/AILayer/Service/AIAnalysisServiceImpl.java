package com.proj1.ailoganlzr.AILayer.Service;

import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.AILayer.Parser.AiParser;
import com.proj1.ailoganlzr.AILayer.Service.Interface.AIAnalysisService;
import com.proj1.ailoganlzr.AILayer.prompt.PromptBuilder;
import com.proj1.ailoganlzr.exception.AIAnalysisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
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
        try{
            log.info("Analyzing stack trace for raw log: {}", rawLog);
            return chatClient.prompt(prompt).user(rawLog).call().entity(AIAnalysisResponse.class);
        }
        catch (Exception e) {
            log.error("Error occurred while analyzing stack trace for raw log: {}", rawLog, e);
            throw new AIAnalysisException("Failed to analyze stack trace", e);
        }
    }
}
