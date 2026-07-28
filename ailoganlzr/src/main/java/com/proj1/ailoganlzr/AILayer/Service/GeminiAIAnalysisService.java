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

            builder.append("Package:\n")
                    .append(snippet.getPackageName())
                    .append("\n\n");

            builder.append("Class:\n")
                    .append(snippet.getClassName())
                    .append("\n\n");

            builder.append("Class Modifiers:\n");
            if (snippet.getClassModifiers().isEmpty()) {
                builder.append("None\n");
            } else {
                snippet.getClassModifiers().forEach(modifier ->
                        builder.append(modifier).append("\n"));
            }

            builder.append("\n");

            builder.append("Implemented Interfaces:\n");
            if (snippet.getImplementedInterfaces().isEmpty()) {
                builder.append("None\n");
            } else {
                snippet.getImplementedInterfaces().forEach(interfaceName ->
                        builder.append(interfaceName).append("\n"));
            }

            builder.append("\n");
            builder.append("Super Class:\n")
                    .append(snippet.getSuperClass() != null ? snippet.getSuperClass() : "None")
                    .append("\n\n");

            builder.append("Fully Qualified Class:\n")
                    .append(snippet.getFullyQualifiedClassName())
                    .append("\n\n");

            builder.append("Annotations:\n");

            snippet.getClassAnnotations().forEach(annotation ->
                    builder.append("@")
                            .append(annotation)
                            .append("\n")
            );

            builder.append("\n");

            builder.append("Fields:\n");

            if (snippet.getFields().isEmpty()) {
                builder.append("None\n");
            } else {
                snippet.getFields()
                        .forEach(field -> builder.append(field).append("\n"));
            }

            builder.append("\n");

            builder.append("Constructor:\n")
                    .append(
                            snippet.getConstructorCode().isBlank()
                                    ? "None"
                                    : snippet.getConstructorCode()
                    )
                    .append("\n\n");

            builder.append("Method:\n")
                    .append(snippet.getMethodName())
                    .append("\n\n");

            builder.append("Line Range:\n")
                    .append(snippet.getStartLine())
                    .append(" - ")
                    .append(snippet.getEndLine())
                    .append("\n\n");

            builder.append("Method Source:\n")
                    .append(snippet.getSourceCode());

            builder.append("\n\n============================================\n\n");
        }

        return builder.toString();
    }
}
