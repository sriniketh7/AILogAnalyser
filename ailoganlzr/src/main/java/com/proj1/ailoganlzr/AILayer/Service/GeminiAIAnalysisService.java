package com.proj1.ailoganlzr.AILayer.Service;

import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.AILayer.Parser.AiParser;
import com.proj1.ailoganlzr.AILayer.Service.Interface.AIAnalysisService;
import com.proj1.ailoganlzr.AILayer.prompt.PromptBuilder;
import com.proj1.ailoganlzr.AILayer.rag.RAGService;
import com.proj1.ailoganlzr.exception.AIAnalysisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeminiAIAnalysisService implements AIAnalysisService {

    private final ChatClient chatClient;

    private final PromptBuilder promptBuilder;

    private final AiParser parser;

    private final RAGService ragService;

    public GeminiAIAnalysisService(ChatClient chatClient, PromptBuilder promptBuilder, AiParser parser, RAGService ragService) {
        this.chatClient = chatClient;
        this.promptBuilder = promptBuilder;
        this.parser = parser;
        this.ragService = ragService;
    }

    @Override
    public AIAnalysisResponse analyzeStackTrace(String rawLog) {

        List<Document> documents =
                ragService.retrieveRelevantDocuments(rawLog);

        String retrievedKnowledge = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n--------------------\n\n"));

        String prompt = promptBuilder.buildStackTracePrompt(rawLog, retrievedKnowledge);
        try{
            log.info("Analyzing stack trace for raw log: {}", rawLog);
            return chatClient.prompt(prompt)
                    .call()
                    .entity(AIAnalysisResponse.class);
        }
        catch (Exception e) {
            log.error("Error occurred while analyzing stack trace for raw log: {}", rawLog, e);
            throw new AIAnalysisException("Failed to analyze stack trace", e);
        }
    }
}
