package com.proj1.ailoganlzr.AILayer.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIAnalysisResponse {

 private String summary;
 private String rootCause;
 private String solution;
 private Integer confidence;

}
