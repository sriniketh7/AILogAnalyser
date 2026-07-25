package com.proj1.ailoganlzr.service.Interface;

import com.proj1.ailoganlzr.DTO.StackFrame;

import java.util.Optional;

public interface CodeSnippetServiceIn {
    Optional<com.proj1.ailoganlzr.DTO.CodeSnippet> extractSnippet(
            StackFrame frame
    );
}
