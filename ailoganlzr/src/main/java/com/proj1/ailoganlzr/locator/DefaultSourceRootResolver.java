package com.proj1.ailoganlzr.locator;

import com.proj1.ailoganlzr.config.AnalyzerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
public class DefaultSourceRootResolver implements SourceRootResolver {

    private final AnalyzerProperties properties;

    public DefaultSourceRootResolver(AnalyzerProperties properties) {
        this.properties = properties;
    }


    @Override
    public Path resolveSourceRoot() {

        String configuredSourceRoot = properties.getSourceRoot();

        // 1. Explicit configuration.
        if (configuredSourceRoot != null && !configuredSourceRoot.isBlank()) {

            Path configured = Paths.get(configuredSourceRoot)
                    .toAbsolutePath()
                    .normalize();

            if (Files.exists(configured)) {
                log.info("Using configured source root: {}", configured);
                return configured;
            }

            throw new IllegalStateException(
                    "Configured source root does not exist: " + configured
            );
        }

        // 2. Auto-discover Maven project root.
        Path userDir = Paths.get(System.getProperty("user.dir"));

        log.info("user.dir = {}", userDir);

        Optional<Path> sourceRoot =
                findDefaultSourceRoot(userDir);

        if (sourceRoot.isPresent()) {
            log.info("Using discovered source root: {}", sourceRoot.get());
            return sourceRoot.get();
        }

        // 3. Production / JAR deployment.
        log.warn("Unable to locate src/main/java. Code extraction will be skipped.");
        return null;
    }



    private Optional<Path> findProjectRoot(Path startDirectory) {

        // Case 1: user.dir itself is the Maven project.
        if (Files.exists(startDirectory.resolve("pom.xml"))) {
            return Optional.of(startDirectory);
        }

        // Case 2: Search child directories (workspace layout).
        try (Stream<Path> paths = Files.walk(startDirectory, 2)) {

            return paths
                    .filter(Files::isDirectory)
                    .filter(path -> Files.exists(path.resolve("pom.xml")))
                    .findFirst();

        } catch (IOException e) {

            log.warn("Failed while searching for Maven project root.", e);
            return Optional.empty();
        }
    }
    private Optional<Path> findDefaultSourceRoot(Path userDir) {

        Optional<Path> projectRoot = findProjectRoot(userDir);

        if (projectRoot.isEmpty()) {
            return Optional.empty();
        }

        Path sourceRoot = projectRoot.get()
                .resolve("src")
                .resolve("main")
                .resolve("java")
                .toAbsolutePath()
                .normalize();

        return Files.exists(sourceRoot)
                ? Optional.of(sourceRoot)
                : Optional.empty();
    }
}