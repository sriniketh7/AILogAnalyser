package com.proj1.ailoganlzr.service;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.proj1.ailoganlzr.DTO.CodeSnippet;
import com.proj1.ailoganlzr.service.Interface.CodeSnippetBuilder;

import java.util.Optional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.springframework.stereotype.Service;

@Service
public class JavaParserCodeSnippetBuilder implements CodeSnippetBuilder {


    @Override
    public CodeSnippet build(CompilationUnit compilationUnit, MethodDeclaration method, int executedLine) {
        Optional<ClassOrInterfaceDeclaration> clazz =
                compilationUnit.findFirst(ClassOrInterfaceDeclaration.class);

        String packageName = compilationUnit.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");

        String className = clazz
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse("");

        List<String> classAnnotations = clazz
                .map(c -> c.getAnnotations()
                        .stream()
                        .map(a -> a.getNameAsString())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        String constructorCode = clazz
                .filter(c -> !c.getConstructors().isEmpty())
                .map(c -> c.getConstructors().get(0).toString())
                .orElse("");

        List<String> fields = clazz
                .map(c -> c.getFields()
                        .stream()
                        .map(FieldDeclaration::toString)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        List<String> modifiers = clazz
                .map(c -> c.getModifiers()
                        .stream()
                        .map(m -> m.getKeyword().asString())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        List<String> interfaces = clazz
                .map(c -> c.getImplementedTypes()
                        .stream()
                        .map(t -> t.getNameAsString())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        String superClass = clazz
                .filter(c -> !c.getExtendedTypes().isEmpty())
                .map(c -> c.getExtendedTypes().get(0).getNameAsString())
                .orElse("");

        int startLine = method.getBegin().map(p -> p.line).orElse(-1);
        int endLine = method.getEnd().map(p -> p.line).orElse(-1);

        String fullyQualifiedClassName = packageName.isEmpty() ? className : packageName + "." + className;

        return CodeSnippet.builder()
                .fullyQualifiedClassName(fullyQualifiedClassName)
                .methodName(method.getNameAsString())
                .startLine(startLine)
                .endLine(endLine)
                .sourceCode(method.toString())
                .packageName(packageName)
                .className(className)
                .classAnnotations(classAnnotations)
                .constructorCode(constructorCode)
                .fields(fields)
                .classModifiers(modifiers)
                .implementedInterfaces(interfaces)
                .superClass(superClass)
                .executedLine(executedLine)
                .methodInvocations(Collections.emptyList())
                .build();

    }
}
