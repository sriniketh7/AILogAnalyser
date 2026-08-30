package com.proj1.ailoganlzr.cache;

import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;

import java.util.Optional;

public interface AnalysisCacheService {

    Optional<AIAnalysisResponse> get(String rawLog);

    void put(String rawLog, AIAnalysisResponse response);

    void evict(String rawLog);
}
