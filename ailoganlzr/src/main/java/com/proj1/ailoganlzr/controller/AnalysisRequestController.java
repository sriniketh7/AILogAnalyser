package com.proj1.ailoganlzr.controller;

import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.CommonResponse.ApiResponse;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResultResponseDto;
import com.proj1.ailoganlzr.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisRequestController {

    private final AnalysisService service;

    @PostMapping
    public ResponseEntity<ApiResponse<AnalysisResultResponseDto>> create(
            @Valid @RequestBody AnalysisRequestDto dto) {

        log.info("Received analysis request: {}", dto);

        AnalysisResultResponseDto analysisResponse = service.analyse(dto);

        ApiResponse<AnalysisResultResponseDto> response =
                new ApiResponse<>(
                        true,
                        "Log analysis completed successfully.",
                        analysisResponse
                );

        log.info("Analysis response: {}", response);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);

    }


}
