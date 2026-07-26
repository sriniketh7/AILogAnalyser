package com.proj1.ailoganlzr.service;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.proj1.ailoganlzr.DTO.CodeSnippet;
import com.proj1.ailoganlzr.DTO.StackFrame;
import com.proj1.ailoganlzr.locator.SourceFileLocator;
import com.proj1.ailoganlzr.service.Interface.CodeSnippetExtractionService;
import com.proj1.ailoganlzr.service.Interface.StackTraceParser;
import org.springframework.stereotype.Service;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class JavaParserCodeSnippetExtractionService implements CodeSnippetExtractionService {

    private final SourceFileLocator sourceFileLocator;
    private final StackTraceParser stackTraceParser;

    public JavaParserCodeSnippetExtractionService(SourceFileLocator sourceFileLocator,StackTraceParser stackTraceParser) {
        this.stackTraceParser = stackTraceParser;
        this.sourceFileLocator = sourceFileLocator;
    }

    @Override
    public Optional<com.proj1.ailoganlzr.DTO.CodeSnippet> extractSnippet(StackFrame frame) {

        Optional<Path> javaFile =
                sourceFileLocator.locate(frame);

        if (javaFile.isEmpty()) {
            return Optional.empty();
        }

        CompilationUnit compilationUnit;

        try {
            compilationUnit =
                    StaticJavaParser.parse(javaFile.get());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

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
                return Optional.of(
                        CodeSnippet.builder()
                                .fullyQualifiedClassName(frame.getFullyQualifiedClassName())
                                .methodName(method.getNameAsString())
                                .startLine(startLine)
                                .endLine(endLine)
                                .sourceCode(method.toString())
                                .build()
                );
            }
        }

        return Optional.empty();
    }

    @Override
    public List<CodeSnippet> extractSnippets(String stackTrace) {
        List<StackFrame> frames = stackTraceParser.parseStackTrace(stackTrace);

        List<CodeSnippet> snippets = new ArrayList<>();

        for (StackFrame frame : frames) {
            extractSnippet(frame).ifPresent(snippets::add);
        }

        return snippets;
    }


}