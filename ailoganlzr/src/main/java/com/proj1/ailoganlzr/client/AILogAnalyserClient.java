package com.proj1.ailoganlzr.client;

import com.proj1.ailoganlzr.DTO.Response.AnalysisResultResponseDto;

public interface AILogAnalyserClient {

    AnalysisResultResponseDto sendAnalysis(Throwable exception);
}
