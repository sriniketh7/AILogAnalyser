package com.proj1.ailoganlzr.service.Interface;

import com.proj1.ailoganlzr.DTO.CodeSnippet;

import java.util.List;

public interface AnalysisContextBuilder {

    String build(
            String rawStackTrace,
            List<CodeSnippet> snippets,
            List<String> knowledgeDocuments
    );

}
