package com.proj1.ailoganlzr.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    private String packageName;

    private String className;

    private List<String> classAnnotations;

    private String constructorCode;

    private List<String> fields;

    private List<String> classModifiers;

    private List<String> implementedInterfaces;

    private String superClass;
}
