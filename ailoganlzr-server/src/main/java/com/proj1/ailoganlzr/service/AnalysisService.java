package com.proj1.ailoganlzr.service;

import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.AILayer.Service.Interface.AIAnalysisService;
import com.proj1.ailoganlzr.AILayer.prompt.PromptVersion;
import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResultResponseDto;
import com.proj1.ailoganlzr.Model.AnalysisRequest;
import com.proj1.ailoganlzr.Model.AnalysisResult;
import com.proj1.ailoganlzr.cache.AnalysisCacheService;
import com.proj1.ailoganlzr.config.AnalyzerProperties;
import com.proj1.ailoganlzr.dao.AnalysisRequestRepository;
import com.proj1.ailoganlzr.enums.AnalysisStatus;
import com.proj1.ailoganlzr.exception.AIAnalysisException;
import com.proj1.ailoganlzr.mapper.AnalysisMapper;
import com.proj1.ailoganlzr.metrics.MetricService;
import com.proj1.ailoganlzr.service.Interface.AnalysisRequestIn;
import io.micrometer.core.instrument.Timer;
import jakarta.transaction.Transactional;
import com.proj1.ailoganlzr.enums.AnalysisType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AnalysisService implements AnalysisRequestIn {

    private final AnalysisRequestRepository requestRepository;
    private final AnalysisMapper requestMapper;
    private final AIAnalysisService aiAnalysisService;
    private final AnalysisCacheService analysisCacheService;
    private final MetricService metricService;
    private final AnalyzerProperties analyzerProperties;


    public AnalysisService(AnalysisRequestRepository requestRepository, AnalysisMapper requestMapper, AIAnalysisService aiAnalysisService, AnalysisCacheService analysisCacheService, MetricService metricService, AnalyzerProperties analyzerProperties) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.aiAnalysisService = aiAnalysisService;
        this.analysisCacheService = analysisCacheService;
        this.metricService = metricService;
        this.analyzerProperties = analyzerProperties;

    }



    @Override
    @Transactional
    public AnalysisResultResponseDto analyse(AnalysisRequestDto requestDto) {

        metricService.incrementAnalysisRequests();
        Timer.Sample analysisSample =
                metricService.startAnalysisTimer();

        // DTO -> Entity
        AnalysisRequest request = requestMapper.toEntity(requestDto);
        log.info("Converted analysis request to entity: {}", request);

        request.setRequestHash(UUID.randomUUID().toString());

        request.setAnalysisType(AnalysisType.STACK_TRACE);

        request.setStatus(AnalysisStatus.PENDING);


        request = requestRepository.save(request);

        log.info("request is saved to database with status PENDING and requestHash: {}", request.getRequestHash());

        long start = System.nanoTime();
        AIAnalysisResponse aiResponse;
        boolean isCached;
        try {
            log.info("Starting AI analysis for request with ID: {}", request.getId());
            String rawLog = request.getRawLog();
            var codeSnippets = requestDto.getCodeSnippets();

            log.info(
                    "Raw log hash for cache lookup: {}",
                    rawLog.hashCode()
            );

            Optional<AIAnalysisResponse> cachedResponse = analysisCacheService.get(rawLog);

            if (cachedResponse.isPresent()) {
                metricService.incrementCacheHit();

                log.info(
                        "Cache HIT for request with ID: {}. Skipping AI analysis.",
                        request.getId()
                );

                aiResponse = cachedResponse.get();
                isCached = true;

            }
            else {
                metricService.incrementCacheMiss();

                log.info(
                        "Cache MISS for request with ID: {}. Calling AI analysis.",
                        request.getId()
                );

                aiResponse = aiAnalysisService.analyzeStackTrace(rawLog, requestDto.getCodeSnippets());

                analysisCacheService.put(rawLog, aiResponse);
                isCached = false;
            }

        }
        catch (Exception e) {
            metricService.incrementAiFailure();
            metricService.stopAnalysisTimer(analysisSample);
            log.error("Error occurred while analyzing request with ID: {}", request.getId(), e);
            request.setStatus(AnalysisStatus.FAILED);
            String message = e.getMessage();
            request.setFailureReason(
                    message != null ? message : e.getClass().getSimpleName()
            );
            request.setCompletedAt(LocalDateTime.now());
            requestRepository.save(request);
            throw new AIAnalysisException("AI analysis failed for request with ID: " + request.getId(), e);
        }

        long processingTime =
                Duration.ofNanos(System.nanoTime() - start).toMillis();

        AnalysisResult result = buildAnalysisResult(aiResponse, processingTime, isCached);
        request.addAnalysisResult(result);


        request.setStatus(AnalysisStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());
        requestRepository.save(request);

        log.info("Analysis Completed and saved to database with status COMPLETED for request with ID: {}", request.getId());

        metricService.stopAnalysisTimer(analysisSample);
        return requestMapper.toResponseDto(result);

    }

    private AnalysisResult buildAnalysisResult(AIAnalysisResponse aiResponse, long processingTime, boolean isCached) {

        AnalysisResult result = new AnalysisResult();

        result.setSummary(aiResponse.getSummary());
        result.setRootCause(aiResponse.getRootCause());
        result.setSolution(aiResponse.getSolution());
        result.setConfidence(aiResponse.getConfidence());

        result.setAiModel(analyzerProperties.getAiModel());
        result.setPromptVersion(PromptVersion.VERSION);
        result.setCached(isCached);
        result.setProcessingTimeMs(processingTime);

        return result;
    }


}
