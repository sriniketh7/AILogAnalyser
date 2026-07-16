package com.proj1.ailoganlzr.service;

import com.proj1.ailoganlzr.AILayer.Model.AIAnalysisResponse;
import com.proj1.ailoganlzr.AILayer.Service.Interface.AIAnalysisService;
import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResultResponseDto;
import com.proj1.ailoganlzr.Model.AnalysisRequest;
import com.proj1.ailoganlzr.Model.AnalysisResult;
import com.proj1.ailoganlzr.dao.AnalysisRequestRepository;
import com.proj1.ailoganlzr.dao.AnalysisResultRepository;
import com.proj1.ailoganlzr.enums.AnalysisStatus;
import com.proj1.ailoganlzr.mapper.AnalysisMapper;
import com.proj1.ailoganlzr.service.Interface.AnalysisRequestIn;
import jakarta.transaction.Transactional;
import com.proj1.ailoganlzr.enums.AnalysisType;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AnalysisService implements AnalysisRequestIn {

    private final AnalysisRequestRepository requestRepository;
    private final AnalysisMapper requestMapper;
    private final AnalysisResultRepository resultRepository;
    private final AIAnalysisService aiAnalysisService;


    public AnalysisService(AnalysisRequestRepository requestRepository, AnalysisMapper requestMapper, AnalysisResultRepository resultRepository, AIAnalysisService aiAnalysisService) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.resultRepository = resultRepository;
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

        AIAnalysisResponse aiResponse =
                aiAnalysisService.analyzeStackTrace(
                        request.getRawLog()
                );

        long end = System.currentTimeMillis();
        long processingTime = end - start;

        AnalysisResult result = buildAnalysisResult(aiResponse, processingTime);
        // Link Both Sides
        request.addAnalysisResult(result);

        // AI Finished
        request.setStatus(AnalysisStatus.COMPLETED);

        // Save Only Parent
        requestRepository.save(request);

        return requestMapper.toResponseDto(result);

    }

    private AnalysisResult buildAnalysisResult(AIAnalysisResponse aiResponse, long processingTime) {

        AnalysisResult result = new AnalysisResult();

        result.setSummary(aiResponse.getSummary());
        result.setRootCause(aiResponse.getRootCause());
        result.setSolution(aiResponse.getSolution());
        result.setConfidence(aiResponse.getConfidence());

        result.setAiModel("gemini-2.5-flash");
        result.setCached(false);
        result.setProcessingTimeMs(processingTime);

        return result;
    }
}
