package com.proj1.ailoganlzr.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class MetricService {

    private final Counter analysisRequestsCounter;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter aiFailureCounter;
    private final Timer analysisTimer;
    private final MeterRegistry meterRegistry;
    private final Timer redisLookupTimer;
    private final Timer ragTimer;
    private final Timer codeExtractionTimer;
    private final Timer geminiTimer;

    public MetricService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        analysisRequestsCounter = Counter.builder("ailoganlzr.analysis.requests.total")
                .description("Total number of analysis requests")
                .register(meterRegistry);

        cacheHitCounter = Counter.builder("ailoganlzr.analysis.cache.hit")
                .description("Total cache hits")
                .register(meterRegistry);

        cacheMissCounter = Counter.builder("ailoganlzr.analysis.cache.miss")
                .description("Total cache misses")
                .register(meterRegistry);

        aiFailureCounter = Counter.builder("ailoganlzr.analysis.ai.failures")
                .description("Total AI analysis failures")
                .register(meterRegistry);
        analysisTimer = Timer.builder("ailoganlzr.analysis.total.time")
                .description("Total analysis request duration")
                .register(meterRegistry);

        redisLookupTimer = Timer.builder("ailoganlzr.analysis.cache.lookup.time")
                .description("Redis cache lookup duration")
                .register(meterRegistry);

        ragTimer = Timer.builder("ailoganlzr.analysis.rag.time")
                .description("RAG similarity search duration")
                .register(meterRegistry);

        codeExtractionTimer = Timer.builder("ailoganlzr.analysis.code.extraction.time")
                .description("JavaParser code extraction duration")
                .register(meterRegistry);

        geminiTimer = Timer.builder("ailoganlzr.analysis.ai.time")
                .description("Gemini API response duration")
                .register(meterRegistry);
    }

    public void incrementAnalysisRequests() {
        analysisRequestsCounter.increment();
    }

    public void incrementCacheHit() {
        cacheHitCounter.increment();
    }

    public void incrementCacheMiss() {
        cacheMissCounter.increment();
    }

    public void incrementAiFailure() {
        aiFailureCounter.increment();
    }
    public Timer.Sample startAnalysisTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopAnalysisTimer(Timer.Sample sample) {
        sample.stop(analysisTimer);
    }

    public Timer.Sample startCodeExtractionTimer() {
    }

    public void stopCodeExtractionTimer(Timer.Sample sample) {
    }

    public Timer.Sample startRedisLookupTimer() {
    }

    public void stopRedisLookupTimer(Timer.Sample sample) {
    }

    public Timer.Sample startGeminiTimer() {
    }

    public void stopRagTimer(Timer.Sample sample) {
    }

    public Timer.Sample startRagTimer() {
    }
}