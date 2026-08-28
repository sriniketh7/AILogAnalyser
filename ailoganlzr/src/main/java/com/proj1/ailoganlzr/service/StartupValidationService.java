package com.proj1.ailoganlzr.service;

import com.proj1.ailoganlzr.config.AnalyzerProperties;
import com.proj1.ailoganlzr.locator.SourceRootResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupValidationService implements ApplicationRunner {

    private final AnalyzerProperties analyzerProperties;
    private final SourceRootResolver sourceRootResolver;

    @Value("${spring.ai.google.genai.api-key:}")
    private String geminiApiKey;

    @Value("${spring.ai.vectorstore.pgvector.table-name:}")
    private String vectorTableName;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {

        validateGemini();
        validateVectorStore();
        validateAnalyzerProperties();
        validateSourceRoot();

        log.info("AILogAnalyser startup validation completed successfully.");
    }

    private void validateGemini() {

        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API key is missing. Configure spring.ai.google.genai.api-key."
            );
        }

        log.info("Gemini API key configured.");
    }

    private void validateVectorStore() {

        if (vectorTableName == null || vectorTableName.isBlank()) {
            throw new IllegalStateException(
                    "PGVector table name is missing."
            );
        }

        log.info("PGVector table configured: {}", vectorTableName);
    }

    private void validateAnalyzerProperties() {

        if (analyzerProperties.getMaxSnippets() <= 0) {
            throw new IllegalStateException("maxSnippets must be greater than zero.");
        }

        log.info("Analyzer properties validated successfully.");
    }

    private void validateSourceRoot() {

        Path sourceRoot = sourceRootResolver.resolveSourceRoot();

        if (sourceRoot == null) {
            log.warn("Source root unavailable. JavaParser code extraction is disabled.");
        } else {
            log.info("JavaParser source root: {}", sourceRoot);
        }
    }
}