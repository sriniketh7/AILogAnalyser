package com.proj1.ailoganlzr.controller;

import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.cache.AnalysisCacheService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/test/cache")
public class RedisCacheTestController {

    private final AnalysisCacheService cacheService;

    public RedisCacheTestController(AnalysisCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping
    public Object get(@RequestParam String rawLog) {

        Optional<AIAnalysisResponse> cached =
                cacheService.get(rawLog);

        return cached.orElse(null);
    }

    @PostMapping
    public String put(@RequestBody CacheTestRequest request) {

        AIAnalysisResponse response =
                new AIAnalysisResponse(
                        request.summary(),
                        request.rootCause(),
                        request.solution(),
                        request.confidence()
                );

        cacheService.put(request.rawLog(), response);

        return "Cached successfully";
    }

    @DeleteMapping
    public String evict(@RequestBody String rawLog) {

        cacheService.evict(rawLog);

        return "Cache evicted";
    }

    public record CacheTestRequest(
            String rawLog,
            String summary,
            String rootCause,
            String solution,
            Integer confidence
    ) {}
}
