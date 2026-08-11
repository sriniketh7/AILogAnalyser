package com.proj1.ailoganlzr.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodInvocationInfo {

    private String callerMethod;

    private String targetObject;

    private String targetMethod;
}
