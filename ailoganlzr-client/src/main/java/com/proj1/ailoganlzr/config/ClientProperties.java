package com.proj1.ailoganlzr.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "ai.loganalyzer.client")
public class ClientProperties {

    private String serverUrl = "http://localhost:9090";

    private Duration connectTimeout = Duration.ofSeconds(5);

    private Duration readTimeout = Duration.ofSeconds(30);

    private String sourceRoot;

    private boolean enabled = true;

}
