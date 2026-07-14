package com.proj1.ailoganlzr.service.Interface;

import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResultResponseDto;

public interface AnalysisRequestIn {
    AnalysisResultResponseDto analyse(
            AnalysisRequestDto requestDto);
}
