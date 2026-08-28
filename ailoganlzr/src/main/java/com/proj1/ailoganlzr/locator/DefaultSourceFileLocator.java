package com.proj1.ailoganlzr.locator;

import com.proj1.ailoganlzr.DTO.StackFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Component
public class DefaultSourceFileLocator implements SourceFileLocator {

    private final SourceRootResolver sourceRootResolver;

    public DefaultSourceFileLocator(SourceRootResolver sourceRootResolver) {
        this.sourceRootResolver = sourceRootResolver;
    }

    @Override
    public Optional<Path> locate(StackFrame frame) {

        Path sourceRoot = sourceRootResolver.resolveSourceRoot();

        if (sourceRoot == null) {
            log.warn("Source root unavailable. Skipping lookup for {}",
                    frame.getFullyQualifiedClassName());
            return Optional.empty();
        }

        Path javaFile = sourceRoot.resolve(
                frame.getFullyQualifiedClassName()
                        .replace('.', '/')
                        + ".java");

        if (Files.exists(javaFile)) {
            return Optional.of(javaFile);
        }

        log.debug("Java source file not found: {}", javaFile);
        return Optional.empty();
    }

    @Override
    public Optional<Path> locateByClassName(String fullyQualifiedClassName) {

        Path sourceRoot = sourceRootResolver.resolveSourceRoot();

        if (sourceRoot == null) {
            log.warn("Source root unavailable. Skipping lookup for {}",
                    fullyQualifiedClassName);
            return Optional.empty();
        }

        Path javaFile = sourceRoot.resolve(
                fullyQualifiedClassName
                        .replace('.', '/')
                        + ".java");

        if (Files.exists(javaFile)) {
            return Optional.of(javaFile);
        }

        log.debug("Java source file not found: {}", javaFile);
        return Optional.empty();
    }
}