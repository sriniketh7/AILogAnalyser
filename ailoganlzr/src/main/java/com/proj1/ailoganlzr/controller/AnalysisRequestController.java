package com.proj1.ailoganlzr.controller;

import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.CommonResponse.ApiResponse;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResponseDto;
import com.proj1.ailoganlzr.service.AnalysisRequestService;
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

    private final AnalysisRequestService service;

    @PostMapping
    public ResponseEntity<ApiResponse<AnalysisResponseDto>> create(
            @Valid @RequestBody AnalysisRequestDto dto) {

        AnalysisResponseDto analysisResponse = service.createAnalysisRequest(dto);

        ApiResponse<AnalysisResponseDto> response =
                new ApiResponse<>(
                        true,
                        "Analysis request created successfully.",
                        analysisResponse
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);

    }

}
