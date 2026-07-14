package com.proj1.ailoganlzr.AILayer.Service.Interface;

import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;

public interface AIAnalysisService {

    AIAnalysisResponse analyzeStackTrace(String rawLog);
}
