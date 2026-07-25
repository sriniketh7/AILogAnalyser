package com.proj1.ailoganlzr.locator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DefaultSourceRootResolver implements SourceRootResolver {

    @Value("${ai.loganalyzer.source-root:}")
    private String configuredSourceRoot;


        @Override
        public Path resolveSourceRoot() {

            System.out.println("user.dir = " + System.getProperty("user.dir"));

            /*
             * User explicitly configured a source root.
             */
            if (!configuredSourceRoot.isBlank()) {

                Path configured = Paths.get(configuredSourceRoot);

                if (Files.exists(configured)) {
                    return configured;
                }

                throw new IllegalStateException(
                        "Configured source root does not exist : " + configured);
            }

            /*
             * Default Spring Boot/Maven project layout.
             */
            Path defaultSourceRoot =
                    Paths.get(System.getProperty("user.dir"))
                            .resolve("src")
                            .resolve("main")
                            .resolve("java");

            System.out.println("Checking path = " + defaultSourceRoot);
            System.out.println("Exists = " + Files.exists(defaultSourceRoot));

            if (Files.exists(defaultSourceRoot)) {
                return defaultSourceRoot;
            }

            throw new IllegalStateException(
                    "Unable to locate src/main/java. " +
                            "Configure ai.loganalyzer.source-root if your project uses a custom layout.");
        }

    }
