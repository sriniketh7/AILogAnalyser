package com.proj1.ailoganlzr.config;

import com.proj1.ailoganlzr.AILogAnalyserClient;
import com.proj1.ailoganlzr.AILogAnalyserExceptionAspect;
import com.proj1.ailoganlzr.AILogAnalyserRestClient;
import com.proj1.ailoganlzr.ExceptionRequestBuilder;
import com.proj1.ailoganlzr.locator.DefaultSourceFileLocator;
import com.proj1.ailoganlzr.locator.DefaultSourceRootResolver;
import com.proj1.ailoganlzr.locator.SourceFileLocator;
import com.proj1.ailoganlzr.locator.SourceRootResolver;
import com.proj1.ailoganlzr.service.*;
import com.proj1.ailoganlzr.service.Interface.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@AutoConfiguration
@EnableConfigurationProperties(ClientProperties.class)
public class AILogAnalyserClientAutoConfiguration {

    @Bean
    public RestClient aiLogAnalyserRestClient(
            ClientProperties properties) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(properties.getReadTimeout());

        return RestClient.builder()
                .baseUrl(properties.getServerUrl())
                .requestFactory(requestFactory)
                .build();
    }


    @Bean
    public ExceptionRequestBuilder exceptionRequestBuilder(
            CodeSnippetExtractionService codeSnippetExtractionService) {

        return new ExceptionRequestBuilder(
                codeSnippetExtractionService
        );
    }

    @Bean
    public AILogAnalyserClient aiLogAnalyserClient(
            RestClient aiLogAnalyserRestClient,
            ExceptionRequestBuilder exceptionRequestBuilder) {

        return new AILogAnalyserRestClient(
                aiLogAnalyserRestClient,
                exceptionRequestBuilder
        );
    }

    @Bean
    public SourceRootResolver sourceRootResolver(
            ClientProperties clientProperties) {

        return new DefaultSourceRootResolver(clientProperties);
    }

    @Bean
    public SourceFileLocator sourceFileLocator(
            SourceRootResolver sourceRootResolver) {

        return new DefaultSourceFileLocator(sourceRootResolver);
    }


    @Bean
    public StackTraceParser stackTraceParser() {
        return new RegexStackTraceParser();
    }

    @Bean
    public MethodInvocationExtractor methodInvocationExtractor() {
        return new JavaParserMethodInvocationExtractor();
    }

    @Bean
    public MethodInvocationResolver methodInvocationResolver(SourceRootResolver sourceRootResolver) {
        return new JavaParserMethodInvocationResolver(sourceRootResolver);
    }

    @Bean
    public CodeSnippetBuilder codeSnippetBuilder() {
        return new JavaParserCodeSnippetBuilder();
    }

    @Bean
    public CodeSnippetExtractionService codeSnippetExtractionService(
            SourceFileLocator sourceFileLocator,
            StackTraceParser stackTraceParser,
            MethodInvocationExtractor methodInvocationExtractor,
            CodeSnippetBuilder codeSnippetBuilder,
            MethodInvocationResolver methodInvocationResolver) {

        return new JavaParserCodeSnippetExtractionService(
                sourceFileLocator,
                stackTraceParser,
                methodInvocationExtractor,
                codeSnippetBuilder,
                methodInvocationResolver
        );
    }

    @Bean
    public AILogAnalyserExceptionAspect aiLogAnalyserExceptionAspect(
            AILogAnalyserClient aiLogAnalyserClient) {


        return new AILogAnalyserExceptionAspect(aiLogAnalyserClient);
    }


}
