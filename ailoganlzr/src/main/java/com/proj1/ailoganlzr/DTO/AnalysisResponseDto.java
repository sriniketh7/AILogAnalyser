package com.proj1.ailoganlzr.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponseDto {

    private Long id;

    private String title;

    private String rawLog;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
