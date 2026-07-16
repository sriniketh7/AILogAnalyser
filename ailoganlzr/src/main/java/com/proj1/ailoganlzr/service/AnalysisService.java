package com.proj1.ailoganlzr.service;

import com.proj1.ailoganlzr.AILayer.Constants.AiConstants;
import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.AILayer.Service.Interface.AIAnalysisService;
import com.proj1.ailoganlzr.AILayer.prompt.PromptVersion;
import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResultResponseDto;
import com.proj1.ailoganlzr.Model.AnalysisRequest;
import com.proj1.ailoganlzr.Model.AnalysisResult;
import com.proj1.ailoganlzr.dao.AnalysisRequestRepository;
import com.proj1.ailoganlzr.enums.AnalysisStatus;
import com.proj1.ailoganlzr.mapper.AnalysisMapper;
import com.proj1.ailoganlzr.service.Interface.AnalysisRequestIn;
import jakarta.transaction.Transactional;
import com.proj1.ailoganlzr.enums.AnalysisType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AnalysisService implements AnalysisRequestIn {

    private final AnalysisRequestRepository requestRepository;
    private final AnalysisMapper requestMapper;
    private final AIAnalysisService aiAnalysisService;


    public AnalysisService(AnalysisRequestRepository requestRepository, AnalysisMapper requestMapper, AIAnalysisService aiAnalysisService) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.aiAnalysisService = aiAnalysisService;
    }



    @Override
    @Transactional
    public AnalysisResultResponseDto analyse(AnalysisRequestDto requestDto) {

        // DTO -> Entity
        AnalysisRequest request = requestMapper.toEntity(requestDto);


        request.setRequestHash(UUID.randomUUID().toString());

        request.setAnalysisType(AnalysisType.STACK_TRACE);

        request.setStatus(AnalysisStatus.PENDING);

        request = requestRepository.save(request);

        long start = System.currentTimeMillis();
        AIAnalysisResponse aiResponse;
        try {
            aiResponse = aiAnalysisService.analyzeStackTrace(request.getRawLog());
        }
        catch (Exception e) {
            request.setStatus(AnalysisStatus.FAILED);
            String message = e.getMessage();
            request.setFailureReason(
                    message != null ? message : e.getClass().getSimpleName()
            );
            request.setCompletedAt(LocalDateTime.now());
            requestRepository.save(request);
            throw e;
        }

        long end = System.currentTimeMillis();
        long processingTime = end - start;

        AnalysisResult result = buildAnalysisResult(aiResponse, processingTime);
        request.addAnalysisResult(result);


        request.setStatus(AnalysisStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());
        requestRepository.save(request);

        return requestMapper.toResponseDto(result);

    }

    private AnalysisResult buildAnalysisResult(AIAnalysisResponse aiResponse, long processingTime) {

        AnalysisResult result = new AnalysisResult();

        result.setSummary(aiResponse.getSummary());
        result.setRootCause(aiResponse.getRootCause());
        result.setSolution(aiResponse.getSolution());
        result.setConfidence(aiResponse.getConfidence());

        result.setAiModel(AiConstants.DEFAULT_MODEL);
        result.setPromptVersion(PromptVersion.VERSION);
        result.setCached(false);
        result.setProcessingTimeMs(processingTime);

        return result;
    }
}
