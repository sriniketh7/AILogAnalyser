package com.proj1.ailoganlzr.controller;

import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.CommonResponse.ApiResponse;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResponseDto;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResultResponseDto;
import com.proj1.ailoganlzr.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisRequestController {

    private final AnalysisService service;

    @PostMapping
    public ResponseEntity<ApiResponse<AnalysisResultResponseDto>> create(
            @Valid @RequestBody AnalysisRequestDto dto) {

        AnalysisResultResponseDto analysisResponse = service.analyse(dto);

        ApiResponse<AnalysisResultResponseDto> response =
                new ApiResponse<>(
                        true,
                        "Log analysis completed successfully.",
                        analysisResponse
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);

    }

}
