package com.proj1.ailoganlzr.AILayer.prompt;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {
    public String buildStackTracePrompt(String rawLog,
                                        String retrievedKnowledge,
                                        String codeSnippetContext) {

        return String.format(
                PromptTemplate.SYSTEM_PROMPT,
                rawLog,
                retrievedKnowledge,
                codeSnippetContext
        );
    }

}
