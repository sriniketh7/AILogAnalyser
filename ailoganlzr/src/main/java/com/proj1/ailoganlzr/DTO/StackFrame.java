package com.proj1.ailoganlzr.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StackFrame {
    private String fullyQualifiedClassName;

    private String className;

    private String methodName;

    private String sourceFile;

    private Integer lineNumber;

    private boolean nativeMethod;

    private boolean unknownSource;
}
