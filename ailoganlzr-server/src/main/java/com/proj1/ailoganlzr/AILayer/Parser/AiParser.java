package com.proj1.ailoganlzr.AILayer.Parser;


import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import org.springframework.stereotype.Component;

@Component
public class AiParser {

    public AIAnalysisResponse parse(String response) {

        AIAnalysisResponse aiResponse = new AIAnalysisResponse();

        aiResponse.setSummary(response);

        return aiResponse;

    }
}
