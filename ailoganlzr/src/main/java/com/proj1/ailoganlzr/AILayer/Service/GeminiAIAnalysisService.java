package com.proj1.ailoganlzr.AILayer.Service;

import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.AILayer.Parser.AiParser;
import com.proj1.ailoganlzr.AILayer.Service.Interface.AIAnalysisService;
import com.proj1.ailoganlzr.AILayer.prompt.PromptBuilder;
import com.proj1.ailoganlzr.AILayer.rag.RAGService;
import com.proj1.ailoganlzr.DTO.CodeSnippet;
import com.proj1.ailoganlzr.exception.AIAnalysisException;
import com.proj1.ailoganlzr.service.Interface.CodeSnippetExtractionService;
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

    private final CodeSnippetExtractionService codeSnippetExtractionService;

    public GeminiAIAnalysisService(ChatClient chatClient, PromptBuilder promptBuilder, AiParser parser, RAGService ragService, CodeSnippetExtractionService codeSnippetExtractionService) {
        this.chatClient = chatClient;
        this.promptBuilder = promptBuilder;
        this.parser = parser;
        this.ragService = ragService;
        this.codeSnippetExtractionService = codeSnippetExtractionService;
    }

    @Override
    public AIAnalysisResponse analyzeStackTrace(String rawLog) {

        List<Document> documents =
                ragService.retrieveRelevantDocuments(rawLog);



        String retrievedKnowledge = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n--------------------\n\n"));

        List<CodeSnippet> codeSnippets =
                codeSnippetExtractionService.extractSnippets(rawLog);

        String codeSnippetContext =
                buildCodeSnippetContext(codeSnippets);

        String prompt = promptBuilder.buildStackTracePrompt(
                rawLog,
                retrievedKnowledge,
                codeSnippetContext
        );

        log.info("Constructed prompt for AI analysis: {}", prompt);
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

    private String buildCodeSnippetContext(List<CodeSnippet> snippets) {

        if (snippets.isEmpty()) {
            return "No application source code could be extracted.";
        }

        StringBuilder builder = new StringBuilder();

        for (CodeSnippet snippet : snippets) {

            builder.append("Class: ")
                    .append(snippet.getFullyQualifiedClassName())
                    .append("\n");

            builder.append("Method: ")
                    .append(snippet.getMethodName())
                    .append("\n");

            builder.append("Lines: ")
                    .append(snippet.getStartLine())
                    .append(" - ")
                    .append(snippet.getEndLine())
                    .append("\n\n");

            builder.append(snippet.getSourceCode());

            builder.append("\n\n");
            builder.append("----------------------------------------");
            builder.append("\n\n");
        }

        return builder.toString();
    }
}
