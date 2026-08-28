package com.proj1.ailoganlzr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "ai.loganalyzer")
public class AnalyzerProperties {
    private Integer maxSnippets = 10;
    private Duration cacheTtlHours = Duration.ofHours(24);
    private String aiModel = "gemini-3.1-flash-lite";

    private String vectorStore = "pgvector";

    private String parserEngine = "javaparser";

}
