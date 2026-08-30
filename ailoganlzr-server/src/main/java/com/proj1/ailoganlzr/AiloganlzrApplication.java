package com.proj1.ailoganlzr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiloganlzrApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiloganlzrApplication.class, args);
	}

}
