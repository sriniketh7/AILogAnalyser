package com.proj1.ailoganlzr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.cache.AnalysisCacheService;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

public class RedisAnalysisCacheService  implements AnalysisCacheService {


    private static final String CACHE_PREFIX = "analysis:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisAnalysisCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {

        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }


    @Override
    public Optional<AIAnalysisResponse> get(String rawLog) {
        String key = buildKey(rawLog);

        String cachedValue =
                redisTemplate.opsForValue().get(key);

        if (cachedValue == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                    objectMapper.readValue(
                            cachedValue,
                            AIAnalysisResponse.class
                    )
            );
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    @Override
    public void put(String rawLog, AIAnalysisResponse response) {

        String key = buildKey(rawLog);

        try {

            String value =
                    objectMapper.writeValueAsString(response);

            redisTemplate.opsForValue().set(
                    key,
                    value,
                    Duration.ofHours(24)
            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to cache AI analysis response",
                    e
            );
        }

    }

    @Override
    public void evict(String rawLog) {

        String key = buildKey(rawLog);
        redisTemplate.delete(key); redisTemplate.delete(
                buildKey(rawLog)
        );
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
