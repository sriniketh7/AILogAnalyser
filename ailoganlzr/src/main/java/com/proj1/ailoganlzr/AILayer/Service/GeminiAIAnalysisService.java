package com.proj1.ailoganlzr.AILayer.Service;

import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.AILayer.Parser.AiParser;
import com.proj1.ailoganlzr.AILayer.Service.Interface.AIAnalysisService;
import com.proj1.ailoganlzr.AILayer.prompt.PromptBuilder;
import com.proj1.ailoganlzr.AILayer.rag.RAGService;
import com.proj1.ailoganlzr.DTO.CodeSnippet;
import com.proj1.ailoganlzr.exception.AIAnalysisException;
import com.proj1.ailoganlzr.metrics.MetricService;
import com.proj1.ailoganlzr.service.Interface.CodeSnippetExtractionService;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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

    private final MetricService metricsService;

    public GeminiAIAnalysisService(ChatClient chatClient, PromptBuilder promptBuilder, AiParser parser, RAGService ragService, CodeSnippetExtractionService codeSnippetExtractionService, MetricService metricsService) {
        this.chatClient = chatClient;
        this.promptBuilder = promptBuilder;
        this.parser = parser;
        this.ragService = ragService;
        this.codeSnippetExtractionService = codeSnippetExtractionService;
        this.metricsService = metricsService;
    }

    @Override
    public AIAnalysisResponse analyzeStackTrace(String rawLog) {


        Timer.Sample sample =
                metricsService.startGeminiTimer();
        List<Document> documents =
                ragService.retrieveRelevantDocuments(rawLog);


        String retrievedKnowledge = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n--------------------\n\n"));

        List<CodeSnippet> codeSnippets =
                codeSnippetExtractionService.extractSnippets(rawLog);


        String executionFlowContext =
                buildExecutionFlowContext(codeSnippets);

        String prompt = promptBuilder.buildStackTracePrompt(
                rawLog,
                retrievedKnowledge,
                executionFlowContext
        );

        log.info("Constructed prompt for AI analysis: {}", prompt);
        try {

            log.info("Analyzing stack trace for raw log: {}", rawLog);
            AIAnalysisResponse response = chatClient.prompt(prompt)
                    .call()
                    .entity(AIAnalysisResponse.class);
            metricsService.stopGeminiTimer(sample);
            return response;
        } catch (Exception e) {
            metricsService.stopGeminiTimer(sample);
            metricsService.incrementAiFailure();
            log.error("Error occurred while analyzing stack trace for raw log: {}", rawLog, e);
            throw new AIAnalysisException("Failed to analyze stack trace", e);
        }
    }


    private String buildExecutionFlowContext(List<CodeSnippet> snippets) {

        if (snippets.isEmpty()) {
            return "No application source code could be extracted.";
        }

        List<CodeSnippet> orderedSnippets = new ArrayList<>(snippets);
        StringBuilder builder = new StringBuilder();

        builder.append("""
                ========================================
                        APPLICATION EXECUTION FLOW
                ========================================
                
                """);

        int totalFrames = orderedSnippets.size();

        for (int i = 0; i < totalFrames; i++) {

            CodeSnippet snippet = orderedSnippets.get(i);

            // -------- Frame Role --------
            String role;

            if (i == 0) {
                role = "ENTRY POINT";
            } else {
                role = "APPLICATION LOGIC";
            }


            builder.append("Frame ")
                    .append(i + 1)
                    .append(" of ")
                    .append(totalFrames)
                    .append(" (")
                    .append(role)
                    .append(")\n");

            builder.append("----------------------------------------\n");

            builder.append("Class: ")
                    .append(snippet.getFullyQualifiedClassName())
                    .append("\n");

            builder.append("Method: ")
                    .append(snippet.getMethodName())
                    .append("\n");

            builder.append("Executed Line: ")
                    .append(snippet.getExecutedLine())
                    .append("\n");

            builder.append("Method Range: ")
                    .append(snippet.getStartLine())
                    .append(" - ")
                    .append(snippet.getEndLine())
                    .append("\n\n");

            builder.append("Package:\n")
                    .append(snippet.getPackageName())
                    .append("\n\n");

            builder.append("Modifiers:\n");

            if (snippet.getClassModifiers().isEmpty()) {
                builder.append("None\n");
            } else {
                snippet.getClassModifiers()
                        .forEach(modifier ->
                                builder.append("- ")
                                        .append(modifier)
                                        .append("\n"));
            }

            builder.append("\n");

            builder.append("Superclass:\n")
                    .append(snippet.getSuperClass().isBlank()
                            ? "None"
                            : snippet.getSuperClass())
                    .append("\n\n");

            builder.append("Implemented Interfaces:\n");

            if (snippet.getImplementedInterfaces().isEmpty()) {
                builder.append("None\n");
            } else {
                snippet.getImplementedInterfaces()
                        .forEach(interfaceName ->
                                builder.append("- ")
                                        .append(interfaceName)
                                        .append("\n"));
            }

            builder.append("\n");

            builder.append("Annotations:\n");

            if (snippet.getClassAnnotations().isEmpty()) {
                builder.append("None\n");
            } else {
                snippet.getClassAnnotations()
                        .forEach(annotation ->
                                builder.append("@")
                                        .append(annotation)
                                        .append("\n"));
            }

            builder.append("\n");

            builder.append("Fields / Dependencies:\n");

            if (snippet.getFields().isEmpty()) {
                builder.append("None\n");
            } else {
                snippet.getFields()
                        .forEach(field ->
                                builder.append(field)
                                        .append("\n"));
            }

            builder.append("\n");

            builder.append("Constructor:\n");

            if (snippet.getConstructorCode().isBlank()) {
                builder.append("None\n");
            } else {
                builder.append(snippet.getConstructorCode());
            }

            builder.append("\n\n");

            builder.append("Method Source:\n");
            builder.append(snippet.getSourceCode());

            builder.append("\n\n");

            if (i < totalFrames - 1) {
                builder.append("""
                        ========================================
                                          ↓
                        ========================================
                        
                        """);
            }
        }

        return builder.toString();
    }
}