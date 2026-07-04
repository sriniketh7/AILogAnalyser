package com.proj1.ailoganlzr.service.Interface;

import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResponseDto;

public interface AnalysisRequestIn {
    AnalysisResponseDto createAnalysisRequest(
            AnalysisRequestDto requestDto);
}
