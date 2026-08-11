package com.proj1.ailoganlzr.service.Interface;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.proj1.ailoganlzr.DTO.MethodInvocationInfo;

import java.util.List;

public interface MethodInvocationExtractor {
    List<MethodInvocationInfo> extract(MethodDeclaration method);
}
