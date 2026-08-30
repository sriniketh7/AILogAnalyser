package com.proj1.ailoganlzr.AILayer.Service.Interface;

import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.DTO.CodeSnippet;

import java.util.List;

public interface AIAnalysisService {

    AIAnalysisResponse analyzeStackTrace(
            String rawLog,
            List<CodeSnippet> codeSnippets
    );
}
