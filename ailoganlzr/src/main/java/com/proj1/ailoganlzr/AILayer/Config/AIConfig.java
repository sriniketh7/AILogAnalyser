package com.proj1.ailoganlzr.AILayer.Config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter() {

        return TokenTextSplitter.builder()
                .build();
    }

    @Bean
    public VectorStore vectorStore() {
        return new VectorStore.Builder()
                .build();
    }
}
