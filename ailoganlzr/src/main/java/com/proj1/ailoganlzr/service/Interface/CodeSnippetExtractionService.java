package com.proj1.ailoganlzr.service.Interface;

import com.proj1.ailoganlzr.DTO.CodeSnippet;
import com.proj1.ailoganlzr.DTO.StackFrame;

import java.util.List;
import java.util.Optional;

public interface CodeSnippetExtractionService {
    Optional<com.proj1.ailoganlzr.DTO.CodeSnippet> extractSnippet(
            StackFrame frame
    );
    List<CodeSnippet> extractSnippets(String stackTrace);
}
