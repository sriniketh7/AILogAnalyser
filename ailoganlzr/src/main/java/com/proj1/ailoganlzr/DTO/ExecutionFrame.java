package com.proj1.ailoganlzr.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionFrame {

    private int frameIndex;

    private StackFrame stackFrame;

    private CodeSnippet codeSnippet;
}
