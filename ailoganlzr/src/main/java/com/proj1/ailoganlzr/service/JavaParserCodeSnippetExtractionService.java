package com.proj1.ailoganlzr.service;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.proj1.ailoganlzr.DTO.*;
import com.proj1.ailoganlzr.locator.SourceFileLocator;
import com.proj1.ailoganlzr.service.Interface.*;
import org.springframework.stereotype.Service;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class JavaParserCodeSnippetExtractionService implements CodeSnippetExtractionService {

    private final SourceFileLocator sourceFileLocator;
    private final StackTraceParser stackTraceParser;
    private final MethodInvocationExtractor methodInvocationExtractor;
    private final CodeSnippetBuilder codeSnippetBuilder;
    private final MethodInvocationResolver methodInvocationResolver;

    public JavaParserCodeSnippetExtractionService(SourceFileLocator sourceFileLocator,StackTraceParser stackTraceParser, MethodInvocationExtractor methodInvocationExtractor, CodeSnippetBuilder codeSnippetBuilder, MethodInvocationResolver methodInvocationResolver) {
        this.stackTraceParser = stackTraceParser;
        this.sourceFileLocator = sourceFileLocator;
        this.methodInvocationExtractor = methodInvocationExtractor;
        this.codeSnippetBuilder = codeSnippetBuilder;
        this.methodInvocationResolver = methodInvocationResolver;
    }

    @Override
    public Optional<CodeSnippet> extractSnippet(StackFrame frame) {

        Optional<Path> javaFile =
                sourceFileLocator.locate(frame);

        System.out.println("Frame = " + frame);
        System.out.println("Java File = " + javaFile);

        if (javaFile.isEmpty()) {
            return Optional.empty();
        }

        CompilationUnit compilationUnit =
                parseCompilationUnit(javaFile.get());


        Optional<MethodDeclaration> targetMethod =
                findTargetMethod(compilationUnit, frame);

        if (targetMethod.isEmpty()) {
            return Optional.empty();
        }

        MethodDeclaration method = targetMethod.get();
        CodeSnippet snippet = codeSnippetBuilder.build(
                compilationUnit,
                method,
                frame.getLineNumber()
        );

        List<MethodInvocationInfo> methodInvocations =
                methodInvocationExtractor.extract(method);

        snippet.setMethodInvocations(methodInvocations);

        ParsedClassContext parsedClassContext =
                ParsedClassContext.builder()
                        .compilationUnit(compilationUnit)
                        .classDeclaration(
                                compilationUnit.findFirst(ClassOrInterfaceDeclaration.class)
                                        .orElseThrow()
                        )
                        .currentMethod(method)
                        .build();

        List<ResolvedMethodInvocation> resolvedInvocations =
                methodInvocationResolver.resolve(
                        methodInvocations,
                        parsedClassContext
                );

        snippet.setResolvedMethodInvocations(
                resolvedInvocations
        );

        return Optional.of(snippet);
    }

    @Override
    public List<CodeSnippet> extractSnippets(String stackTrace) {

        List<StackFrame> frames =
                stackTraceParser.parseStackTrace(stackTrace);

        List<CodeSnippet> snippets = new ArrayList<>();

        Set<String> visited = new HashSet<>();

        final int MAX_SNIPPETS = 5;

        for (StackFrame frame : frames) {

            if (snippets.size() >= MAX_SNIPPETS) {
                break;
            }

            Optional<CodeSnippet> optionalSnippet =
                    extractSnippet(frame);

            if (optionalSnippet.isEmpty()) {
                continue;
            }

            CodeSnippet snippet = optionalSnippet.get();

            String key =
                    snippet.getFullyQualifiedClassName()
                            + "#"
                            + snippet.getMethodName();

            if (!visited.add(key)) {
                continue;
            }

            // Add the method from the stack trace
            snippets.add(snippet);

            // Now recursively follow its business-code dependencies
            List<ResolvedMethodInvocation> resolvedInvocations =
                    snippet.getResolvedMethodInvocations();

            if (resolvedInvocations == null) {
                continue;
            }

            for (ResolvedMethodInvocation resolved :
                    resolvedInvocations) {

                if (snippets.size() >= MAX_SNIPPETS) {
                    break;
                }

                if (resolved.getFullyQualifiedClassName() == null ||
                        resolved.getInvocation() == null) {
                    continue;
                }

                String targetClass =
                        resolved.getFullyQualifiedClassName();

                String targetMethod =
                        resolved.getInvocation().getTargetMethod();

                List<CodeSnippet> recursiveSnippets =
                        extractRecursively(
                                targetClass,
                                targetMethod
                        );

                for (CodeSnippet recursiveSnippet :
                        recursiveSnippets) {

                    if (snippets.size() >= MAX_SNIPPETS) {
                        break;
                    }

                    String recursiveKey =
                            recursiveSnippet.getFullyQualifiedClassName()
                                    + "#"
                                    + recursiveSnippet.getMethodName();

                    if (visited.add(recursiveKey)) {
                        snippets.add(recursiveSnippet);
                    }
                }
            }
        }

        return snippets;
    }



    private CompilationUnit parseCompilationUnit(Path javaFile) {

        try {
            ParserConfiguration configuration =
                    new ParserConfiguration()
                            .setLanguageLevel(
                                    ParserConfiguration.LanguageLevel.JAVA_26
                            );

            return new com.github.javaparser.JavaParser(configuration)
                    .parse(javaFile)
                    .getResult()
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Unable to parse Java file: " + javaFile
                            )
                    );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to read Java file: " + javaFile,
                    e
            );
        }
    }


    private Optional<MethodDeclaration> findTargetMethod(
            CompilationUnit compilationUnit,
            StackFrame frame) {

        List<MethodDeclaration> methods =
                compilationUnit.findAll(MethodDeclaration.class);

        for (MethodDeclaration method : methods) {

            if (method.getBegin().isEmpty() || method.getEnd().isEmpty()) {
                continue;
            }

            int startLine = method.getBegin().get().line;
            int endLine = method.getEnd().get().line;

            if (frame.getLineNumber() >= startLine &&
                    frame.getLineNumber() <= endLine) {

                return Optional.of(method);
            }
        }

        return Optional.empty();
    }



    public Optional<CodeSnippet> extractByClassAndMethod(
            String fullyQualifiedClassName,
            String methodName) {

        Optional<Path> javaFile =
                sourceFileLocator.locateByClassName(fullyQualifiedClassName);

        if (javaFile.isEmpty()) {
            return Optional.empty();
        }

        CompilationUnit compilationUnit =
                parseCompilationUnit(javaFile.get());

        Optional<MethodDeclaration> targetMethod =
                compilationUnit.findAll(MethodDeclaration.class)
                        .stream()
                        .filter(method ->
                                method.getNameAsString().equals(methodName))
                        .findFirst();

        if (targetMethod.isEmpty()) {
            return Optional.empty();
        }

        MethodDeclaration method = targetMethod.get();

        CodeSnippet snippet =
                codeSnippetBuilder.build(
                        compilationUnit,
                        method,
                        method.getBegin()
                                .map(position -> position.line)
                                .orElse(0)
                );

        List<MethodInvocationInfo> invocations =
                methodInvocationExtractor.extract(method);

        snippet.setMethodInvocations(invocations);

        ParsedClassContext context =
                ParsedClassContext.builder()
                        .compilationUnit(compilationUnit)
                        .classDeclaration(
                                compilationUnit.findFirst(
                                        ClassOrInterfaceDeclaration.class
                                ).orElseThrow()
                        )
                        .currentMethod(method)
                        .build();

        List<ResolvedMethodInvocation> resolvedInvocations =
                methodInvocationResolver.resolve(
                        invocations,
                        context
                );

        snippet.setResolvedMethodInvocations(resolvedInvocations);

        return Optional.of(snippet);
    }


    @Override
    public List<CodeSnippet> extractRecursively(
            String fullyQualifiedClassName,
            String methodName) {

        List<CodeSnippet> result = new ArrayList<>();

        Set<String> visited = new HashSet<>();

        extractRecursively(
                fullyQualifiedClassName,
                methodName,
                result,
                visited
        );

        return result;
    }


    private void extractRecursively(
            String fullyQualifiedClassName,
            String methodName,
            List<CodeSnippet> result,
            Set<String> visited) {

        String key =
                fullyQualifiedClassName + "#" + methodName;

        // Prevent circular dependencies / duplicate extraction
        if (!visited.add(key)) {
            return;
        }

        Optional<CodeSnippet> optionalSnippet =
                extractByClassAndMethod(
                        fullyQualifiedClassName,
                        methodName
                );

        if (optionalSnippet.isEmpty()) {
            return;
        }

        CodeSnippet snippet = optionalSnippet.get();

        result.add(snippet);

        List<ResolvedMethodInvocation> resolvedInvocations =
                snippet.getResolvedMethodInvocations();

        if (resolvedInvocations == null) {
            return;
        }

        for (ResolvedMethodInvocation resolved :
                resolvedInvocations) {

            if (resolved.getFullyQualifiedClassName() == null) {
                continue;
            }

            MethodInvocationInfo invocation =
                    resolved.getInvocation();

            if (invocation == null) {
                continue;
            }

            String targetClass =
                    resolved.getFullyQualifiedClassName();

            String targetMethod =
                    invocation.getTargetMethod();

            extractRecursively(
                    targetClass,
                    targetMethod,
                    result,
                    visited
            );
        }
    }

    
}