package com.proj1.ailoganlzr.cache;

import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.DTO.CodeSnippet;

import java.util.List;
import java.util.Optional;

public interface AnalysisCacheService {

    Optional<AIAnalysisResponse> get(
            String rawLog,
            List<CodeSnippet> codeSnippets
    );

    void put(
            String rawLog,
            List<CodeSnippet> codeSnippets,
            AIAnalysisResponse response
    );

    void evict(
            String rawLog,
            List<CodeSnippet> codeSnippets
    );
}
