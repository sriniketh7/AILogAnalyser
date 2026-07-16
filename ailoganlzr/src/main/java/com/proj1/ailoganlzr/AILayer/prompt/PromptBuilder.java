package com.proj1.ailoganlzr.AILayer.prompt;

import com.proj1.ailoganlzr.AILayer.Constants.AiConstants;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {
    public String buildStackTracePrompt(String rawLog) {

        return PromptTemplate.SYSTEM_PROMPT.formatted(rawLog);
    }

}
