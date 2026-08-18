package com.proj1.ailoganlzr.service;

import com.proj1.ailoganlzr.DTO.StackFrame;
import com.proj1.ailoganlzr.service.Interface.StackTraceParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RegexStackTraceParser implements StackTraceParser {

    @Override
    public List<StackFrame> parseStackTrace(String stackTrace) {

        List<StackFrame> frames = new ArrayList<>();

        if (stackTrace == null || stackTrace.isBlank()) {
            return frames;
        }

        String[] lines = stackTrace.split("\\R");

        for (String line : lines) {

            parseLine(line).ifPresent(frames::add);

        }

        return frames;
    }

    private Optional<StackFrame> parseLine(String line) {

        line = line.trim();

        if (!line.startsWith("at ")) {
            return Optional.empty();
        }

        line = line.substring(3);

        int openingBracket = line.indexOf('(');

        if (openingBracket == -1) {
            return Optional.empty();
        }

        int closingBracket = line.indexOf(')', openingBracket);

        if (closingBracket == -1) {
            return Optional.empty();
        }

        String methodPart = line.substring(0, openingBracket);

        String sourcePart = line.substring(
                openingBracket + 1,
                closingBracket
        ).trim();

        // Handle Java 9+ module prefix
        if (methodPart.contains("/")) {
            methodPart = methodPart.substring(
                    methodPart.indexOf('/') + 1
            );
        }

        return Optional.of(buildStackFrame(methodPart, sourcePart));
    }

    private StackFrame buildStackFrame(String methodPart, String sourcePart) {

        int lastDot = methodPart.lastIndexOf('.');

        String fullyQualifiedClassName =
                methodPart.substring(0, lastDot);

        String methodName =
                methodPart.substring(lastDot + 1);

        int classDot =
                fullyQualifiedClassName.lastIndexOf('.');

        String className =
                fullyQualifiedClassName.substring(classDot + 1);

        String sourceFile = null;
        Integer lineNumber = null;

        boolean nativeMethod = false;
        boolean unknownSource = false;

        if ("Native Method".equals(sourcePart)) {

            nativeMethod = true;

        } else if ("Unknown Source".equals(sourcePart)) {

            unknownSource = true;

        } else {

            int colonIndex = sourcePart.lastIndexOf(':');

            if (colonIndex != -1) {

                sourceFile =
                        sourcePart.substring(0, colonIndex);

                try {

                    lineNumber =
                            Integer.parseInt(
                                    sourcePart.substring(colonIndex + 1));

                } catch (NumberFormatException ignored) {
                }

            } else {

                sourceFile = sourcePart;

            }
        }

        return StackFrame.builder()
                .fullyQualifiedClassName(fullyQualifiedClassName)
                .className(className)
                .methodName(methodName)
                .sourceFile(sourceFile)
                .lineNumber(lineNumber)
                .nativeMethod(nativeMethod)
                .unknownSource(unknownSource)
                .build();
    }
}