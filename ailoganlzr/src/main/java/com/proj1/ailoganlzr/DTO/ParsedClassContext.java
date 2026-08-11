package com.proj1.ailoganlzr.DTO;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.*;

import java.nio.file.Path;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParsedClassContext {

    private CompilationUnit compilationUnit;

    private ClassOrInterfaceDeclaration classDeclaration;

    private MethodDeclaration currentMethod;

    private Path sourceFile;

    private String packageName;

    private String fullyQualifiedClassName;
}