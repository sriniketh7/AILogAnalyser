package com.proj1.ailoganlzr.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class MetricService {

    // -------- Counters --------
    private final Counter analysisRequestsCounter;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter aiFailureCounter;

    // -------- Timers --------
    private final Timer analysisTimer;
    private final Timer redisLookupTimer;
    private final Timer ragTimer;
    private final Timer codeExtractionTimer;
    private final Timer geminiTimer;

    private final MeterRegistry meterRegistry;

    public MetricService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // -------- Counter Registration --------
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

        // -------- Timer Registration --------
        analysisTimer = Timer.builder("ailoganlzr.analysis.total.time")
                .description("Total analysis request duration")
                .register(meterRegistry);

        redisLookupTimer = Timer.builder("ailoganlzr.analysis.cache.lookup.time")
                .description("Redis cache lookup duration")
                .register(meterRegistry);

        ragTimer = Timer.builder("ailoganlzr.analysis.rag.time")
                .description("PGVector similarity search duration")
                .register(meterRegistry);

        codeExtractionTimer = Timer.builder("ailoganlzr.analysis.code.extraction.time")
                .description("JavaParser code extraction duration")
                .register(meterRegistry);

        geminiTimer = Timer.builder("ailoganlzr.analysis.ai.time")
                .description("Gemini API response duration")
                .register(meterRegistry);
    }

    // ================= COUNTERS =================

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

    // ================= ANALYSIS TIMER =================

    public Timer.Sample startAnalysisTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopAnalysisTimer(Timer.Sample sample) {
        sample.stop(analysisTimer);
    }

    // ================= REDIS TIMER =================

    public Timer.Sample startRedisLookupTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopRedisLookupTimer(Timer.Sample sample) {
        sample.stop(redisLookupTimer);
    }

    // ================= RAG TIMER =================

    public Timer.Sample startRagTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopRagTimer(Timer.Sample sample) {
        sample.stop(ragTimer);
    }

    // ================= CODE EXTRACTION TIMER =================

    public Timer.Sample startCodeExtractionTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopCodeExtractionTimer(Timer.Sample sample) {
        sample.stop(codeExtractionTimer);
    }

    // ================= GEMINI TIMER =================

    public Timer.Sample startGeminiTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopGeminiTimer(Timer.Sample sample) {
        sample.stop(geminiTimer);
    }
}