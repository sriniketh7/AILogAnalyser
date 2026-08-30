package com.proj1.ailoganlzr.service;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.proj1.ailoganlzr.DTO.MethodInvocationInfo;
import com.proj1.ailoganlzr.service.Interface.MethodInvocationExtractor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


public class JavaParserMethodInvocationExtractor implements MethodInvocationExtractor {


    @Override
    public List<MethodInvocationInfo> extract(MethodDeclaration method) {
        List<MethodInvocationInfo> invocations = new ArrayList<>();

        List<MethodCallExpr> methodCalls =
                method.findAll(MethodCallExpr.class);

        for (MethodCallExpr call : methodCalls) {

            MethodInvocationInfo info = MethodInvocationInfo.builder()
                    .callerMethod(method.getNameAsString())
                    .targetMethod(call.getNameAsString())
                    .targetObject(
                            call.getScope()
                                    .map(Object::toString)
                                    .orElse("")
                    )
                    .build();

            invocations.add(info);
        }

        return invocations;
    }
}
