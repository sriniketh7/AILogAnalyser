package com.proj1.ailoganlzr.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultResponseDto {

    private Long requestId;

    private String status;

    private String title;

    private String summary;

    private String rootCause;

    private String solution;

    private Integer confidence;

    private Boolean cached;

}
