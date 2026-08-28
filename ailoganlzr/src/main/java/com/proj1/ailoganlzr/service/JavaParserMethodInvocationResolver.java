package com.proj1.ailoganlzr.service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.proj1.ailoganlzr.DTO.MethodInvocationInfo;
import com.proj1.ailoganlzr.DTO.ParsedClassContext;
import com.proj1.ailoganlzr.DTO.ResolvedMethodInvocation;
import com.proj1.ailoganlzr.locator.SourceRootResolver;
import com.github.javaparser.ParserConfiguration;
import com.proj1.ailoganlzr.service.Interface.MethodInvocationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class JavaParserMethodInvocationResolver
        implements MethodInvocationResolver {

    private final ParserConfiguration parserConfiguration;
    private final SourceRootResolver sourceRootResolver;

    public JavaParserMethodInvocationResolver(
            SourceRootResolver sourceRootResolver
    ) {
        this.sourceRootResolver = sourceRootResolver;

        this.parserConfiguration =
                new ParserConfiguration()
                        .setLanguageLevel(
                                ParserConfiguration.LanguageLevel.JAVA_26
                        );
    }

    @Override
    public List<ResolvedMethodInvocation> resolve(
            List<MethodInvocationInfo> invocations,
            ParsedClassContext context
    ) {

        List<ResolvedMethodInvocation> resolved = new ArrayList<>();

        for (MethodInvocationInfo invocation : invocations) {

            Optional<String> targetType =
                    resolveTargetType(
                            context,
                            invocation
                    );

            if (targetType.isEmpty()) {
                continue;
            }

            Optional<String> fullyQualifiedClassName =
                    findFullyQualifiedClassName(
                            targetType.get()
                    );

            if (fullyQualifiedClassName.isEmpty()) {
                continue;
            }

            resolved.add(
                    ResolvedMethodInvocation.builder()
                            .invocation(invocation)
                            .fullyQualifiedClassName(
                                    fullyQualifiedClassName.get()
                            )
                            .build()
            );
        }

        return resolved;
    }
    private Optional<String> resolveFieldType(
            ParsedClassContext context,
            String fieldName
    ) {

        ClassOrInterfaceDeclaration clazz =
                context.getClassDeclaration();

        for (FieldDeclaration field : clazz.getFields()) {

            for (VariableDeclarator variable : field.getVariables()) {

                if (variable.getNameAsString().equals(fieldName)) {

                    return Optional.of(
                            variable.getType().asString()
                    );

                }

            }

        }

        return Optional.empty();
    }





    private Optional<String> resolveTargetType(
            ParsedClassContext context,
            MethodInvocationInfo invocation
    ) {

        // Field
        Optional<String> type =
                resolveFieldType(
                        context,
                        invocation.getTargetObject()
                );

        if (type.isPresent()) {
            return type;
        }

        // Parameter
        type =
                resolveParameterType(
                        context,
                        invocation.getTargetObject()
                );

        if (type.isPresent()) {
            return type;
        }

        // Local variable
        type =
                resolveLocalVariableType(
                        context,
                        invocation.getTargetObject()
                );

        if (type.isPresent()) {
            return type;
        }

        // Same-class invocation
        type =
                resolveSameClassInvocation(
                        context,
                        invocation
                );

        if (type.isPresent()) {
            return type;
        }

        // Static invocation
        type =
                resolveStaticInvocation(
                        invocation.getTargetObject()
                );

        return type;
    }

    private Optional<String> resolveParameterType(
            ParsedClassContext context,
            String parameterName
    ) {

        return context.getCurrentMethod()
                .getParameters()
                .stream()
                .filter(parameter ->
                        parameter.getNameAsString().equals(parameterName))
                .map(parameter ->
                        parameter.getType().asString())
                .findFirst();
    }



    private Optional<String> resolveLocalVariableType(
            ParsedClassContext context,
            String variableName
    ) {
        return context.getCurrentMethod()
            .findAll(VariableDeclarator.class)
            .stream()
            .filter(variable ->
                    variable.getNameAsString().equals(variableName))
            .map(variable ->
                    variable.getType().asString())
            .findFirst();
    }


    private Optional<String> resolveSameClassInvocation(
            ParsedClassContext context,
            MethodInvocationInfo invocation
    ) {

        String targetObject = invocation.getTargetObject();

        if (targetObject == null
                || targetObject.isBlank()
                || "this".equals(targetObject)) {
            return Optional.ofNullable(
                    context.getFullyQualifiedClassName()
            );
        }

        return Optional.empty();
    }


    private Optional<String> resolveStaticInvocation(
            String targetObject
    ) {

        if (targetObject == null || targetObject.isBlank()) {
            return Optional.empty();
        }

        /*
         * Static invocations generally start with
         * an uppercase class name.
         *
         * Examples:
         *
         * UUID.randomUUID()
         * LocalDateTime.now()
         * PromptUtils.buildPrompt()
         */

        if (Character.isUpperCase(targetObject.charAt(0))) {
            return Optional.of(targetObject);
        }

        return Optional.empty();
    }


    private Optional<String> findFullyQualifiedClassName(
            String targetType
    ) {

        Path sourceRoot =
                sourceRootResolver.resolveSourceRoot();

        try {

            return Files.walk(sourceRoot)
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString().endsWith(".java"))
                    .map(this::parseCompilationUnit)
                    .map(compilationUnit ->
                            findTargetType(
                                    compilationUnit,
                                    targetType
                            )
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(
                            clazz -> resolveImplementationIfRequired(clazz)
                                    .stream()
                    )
                    .findFirst();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to search source files for type: "
                            + targetType,
                    e
            );
        }
    }

    private Optional<ClassOrInterfaceDeclaration> findTargetType(
            CompilationUnit compilationUnit,
            String targetType
    ) {

        return compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                .filter(clazz ->
                        clazz.getNameAsString()
                                .equals(targetType))
                .findFirst();
    }

    private Optional<String> resolveImplementationIfRequired(
            ClassOrInterfaceDeclaration targetType
    ) {

        // Normal class
        if (!targetType.isInterface()) {

            CompilationUnit compilationUnit =
                    targetType
                            .findCompilationUnit()
                            .orElseThrow();

            String packageName =
                    compilationUnit
                            .getPackageDeclaration()
                            .map(pd -> pd.getNameAsString())
                            .orElse("");

            String className =
                    targetType.getNameAsString();

            String fullyQualifiedName =
                    packageName.isBlank()
                            ? className
                            : packageName + "." + className;

            return Optional.of(fullyQualifiedName);
        }

        // Interface
        return findImplementationClass(
                targetType.getNameAsString()
        );
    }


    private CompilationUnit parseCompilationUnit(Path javaFile) {

        try {

            JavaParser parser =
                    new JavaParser(parserConfiguration);

            return parser.parse(javaFile)
                    .getResult()
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Unable to parse source file: "
                                            + javaFile
                            )
                    );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to read source file: " + javaFile,
                    e
            );
        }
    }
    private Optional<String> findImplementationClass(
            String interfaceName
    ) {

        log.info(
                "Looking for implementation of interface: {}",
                interfaceName
        );

        Path sourceRoot = sourceRootResolver.resolveSourceRoot();


        try {

            return Files.walk(sourceRoot)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::parseCompilationUnit)
                    .map(compilationUnit ->
                            findImplementationInCompilationUnit(
                                    compilationUnit,
                                    interfaceName
                            )
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to search for implementation of: "
                            + interfaceName,
                    e
            );
        }
    }

    private Optional<String> findImplementationInCompilationUnit(
            CompilationUnit compilationUnit,
            String interfaceName
    ) {

        Optional<ClassOrInterfaceDeclaration> clazz =
                compilationUnit.findFirst(
                        ClassOrInterfaceDeclaration.class
                );

        if (clazz.isEmpty()) {
            return Optional.empty();
        }

        ClassOrInterfaceDeclaration declaration =
                clazz.get();

        boolean implementsInterface =
                declaration.getImplementedTypes()
                        .stream()
                        .anyMatch(type ->
                                type.getNameAsString()
                                        .equals(interfaceName)
                        );

        if (!implementsInterface) {
            return Optional.empty();
        }

        String packageName =
                compilationUnit.getPackageDeclaration()
                        .map(pd -> pd.getNameAsString())
                        .orElse("");

        String className =
                declaration.getNameAsString();

        if (packageName.isBlank()) {
            return Optional.of(className);
        }

       log.info(
                "Found implementation of {}: {}.{}",
                interfaceName,
                packageName,
                className
        );

        return Optional.of(
                packageName + "." + className
        );
    }



}
