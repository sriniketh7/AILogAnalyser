package com.proj1.ailoganlzr.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnalysisRequestDto {
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Raw log cannot be blank")
    private String rawLog;
}
