package com.proj1.ailoganlzr.AILayer.prompt;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {
    public String buildStackTracePrompt(String rawLog, String retrievedKnowledge) {

        return PromptTemplate.SYSTEM_PROMPT.formatted(retrievedKnowledge, rawLog);
    }

}
