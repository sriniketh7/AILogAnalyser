package com.proj1.ailoganlzr.service;

import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResponseDto;
import com.proj1.ailoganlzr.Model.AnalysisRequest;
import com.proj1.ailoganlzr.dao.AnalysisRequestRepository;
import com.proj1.ailoganlzr.mapper.AnalysisRequestMapper;
import com.proj1.ailoganlzr.service.Interface.AnalysisRequestIn;
import org.springframework.stereotype.Service;

@Service
public class AnalysisRequestService implements AnalysisRequestIn {

    private final AnalysisRequestRepository requestRepository;
    private final AnalysisRequestMapper requestMapper;


    public AnalysisRequestService(AnalysisRequestRepository requestRepository,AnalysisRequestMapper requestMapper) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
    }




    @Override
    public AnalysisResponseDto createAnalysisRequest(AnalysisRequestDto requestDto) {

        AnalysisRequest entity = requestMapper.toEntity(requestDto);

        AnalysisRequest saved = requestRepository.save(entity);

        return requestMapper.toDto(saved);

    }
}
