package com.proj1.ailoganlzr.exception;

import com.proj1.ailoganlzr.DTO.CommonResponse.ApiResponse;
import com.proj1.ailoganlzr.DTO.Response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalexceptionHandler {

    @ExceptionHandler(AIAnalysisException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAIAnalysisException(
           AIAnalysisException ex) {
        log.error("AI analysis failed", ex);

        ErrorResponse error = new ErrorResponse(
                "AI_SERVICE_UNAVAILABLE",
                ex.getMessage(),
                LocalDateTime.now()
        );

        ApiResponse<ErrorResponse> response =
                new ApiResponse<>(
                        false,
                        "AI analysis failed",
                        error
                );

        return ResponseEntity
                .status(503)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGenericException(Exception ex) {
        log.error("Unexpected exception occurred", ex);

        ErrorResponse error = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                ex.getMessage(),
                LocalDateTime.now()
        );

        ApiResponse<ErrorResponse> response =
                new ApiResponse<>(false, "An error occurred", error);

        return ResponseEntity.status(500).body(response);
    }
}
