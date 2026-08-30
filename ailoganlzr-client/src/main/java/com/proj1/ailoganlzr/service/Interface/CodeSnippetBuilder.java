package com.proj1.ailoganlzr.service.Interface;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.proj1.ailoganlzr.DTO.CodeSnippet;

public interface CodeSnippetBuilder {

    CodeSnippet build(
            CompilationUnit compilationUnit,
            MethodDeclaration method,
            int executedLine
    );
}
