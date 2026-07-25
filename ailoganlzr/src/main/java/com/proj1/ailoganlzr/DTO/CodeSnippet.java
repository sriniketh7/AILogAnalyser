package com.proj1.ailoganlzr.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSnippet {

    private String fullyQualifiedClassName;

    private String methodName;

    private Integer startLine;

    private Integer endLine;

    private String sourceCode;
}
