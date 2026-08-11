package com.proj1.ailoganlzr.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.nio.file.Path;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResolvedMethodInvocation {

    private MethodInvocationInfo invocation;

    private String fullyQualifiedClassName;


}
