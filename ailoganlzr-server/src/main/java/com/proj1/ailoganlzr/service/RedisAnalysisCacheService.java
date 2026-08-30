package com.proj1.ailoganlzr.service;


import com.proj1.ailoganlzr.config.AnalyzerProperties;
import com.proj1.ailoganlzr.metrics.MetricService;
import io.micrometer.core.instrument.Timer;
import tools.jackson.databind.ObjectMapper;
import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.cache.AnalysisCacheService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class RedisAnalysisCacheService  implements AnalysisCacheService {


    private static final String CACHE_PREFIX = "analysis:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MetricService metricsService;
    private final AnalyzerProperties analyzerProperties;

    public RedisAnalysisCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            MetricService metricsService,
            AnalyzerProperties analyzerProperties) {

        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.metricsService = metricsService;
        this.analyzerProperties = analyzerProperties;
    }


    @Override
    public Optional<AIAnalysisResponse> get(String rawLog) {

        String key = buildKey(rawLog);

        Timer.Sample sample = metricsService.startRedisLookupTimer();

        try {
            String cachedValue =
                    redisTemplate.opsForValue().get(key);

            if (cachedValue == null) {
                return Optional.empty();
            }



            AIAnalysisResponse response =
                    objectMapper.readValue(
                            cachedValue,
                            AIAnalysisResponse.class
                    );


            return Optional.of(response);

        } catch (Exception e) {



            e.printStackTrace();

            return Optional.empty();

        } finally {
            metricsService.stopRedisLookupTimer(sample);
        }
    }

    @Override
    public void put(
            String rawLog,
            AIAnalysisResponse response) {

        String key = buildKey(rawLog);

        try {

            String value =
                    objectMapper.writeValueAsString(response);



            redisTemplate.opsForValue().set(
                    key,
                    value,
                    analyzerProperties.getCacheTtlHours()
            );



        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to cache AI analysis response",
                    e
            );
        }
    }

    @Override
    public void evict(String rawLog) {

        String key = buildKey(rawLog);
        redisTemplate.delete(key);
    }


    private String buildKey(String rawLog) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            rawLog.getBytes(StandardCharsets.UTF_8)
                    );

            return CACHE_PREFIX +
                    HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    e
            );
        }
    }
}
