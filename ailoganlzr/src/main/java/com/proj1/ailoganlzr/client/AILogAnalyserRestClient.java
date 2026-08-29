package com.proj1.ailoganlzr.client;

import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.CommonResponse.ApiResponse;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResultResponseDto;
import com.proj1.ailoganlzr.client.exception.AILogAnalyserClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class AILogAnalyserRestClient implements AILogAnalyserClient {

    private final RestClient restClient;
    private final ExceptionRequestBuilder exceptionRequestBuilder;

    public AILogAnalyserRestClient(
            RestClient aiLogAnalyserRestClient,
            ExceptionRequestBuilder exceptionRequestBuilder) {

        this.restClient = aiLogAnalyserRestClient;
        this.exceptionRequestBuilder = exceptionRequestBuilder;
    }

    @Override
    public AnalysisResultResponseDto sendAnalysis(Throwable exception) {

        AnalysisRequestDto request =
                exceptionRequestBuilder.build(exception);

        try {

            ApiResponse<AnalysisResultResponseDto> response =
                    restClient.post()
                            .uri("/analysis")
                            .body(request)
                            .retrieve()
                            .body(new ParameterizedTypeReference<ApiResponse<AnalysisResultResponseDto>>() {});

            if (response == null || response.getData() == null) {
                throw new AILogAnalyserClientException(
                        "Received empty response from AILogAnalyser server."
                );
            }

            log.info("Received AI analysis successfully.");

            return response.getData();

        } catch (org.springframework.web.client.HttpClientErrorException ex) {

            throw new AILogAnalyserClientException(
                    "Invalid request sent to AILogAnalyser server. Status: "
                            + ex.getStatusCode(),
                    ex
            );

        } catch (org.springframework.web.client.HttpServerErrorException ex) {

            throw new AILogAnalyserClientException(
                    "AILogAnalyser server encountered an internal error. Status: "
                            + ex.getStatusCode(),
                    ex
            );

        } catch (org.springframework.web.client.ResourceAccessException ex) {

            throw new AILogAnalyserClientException(
                    "Unable to connect to AILogAnalyser server. Check server URL or network connectivity.",
                    ex
            );

        } catch (Exception ex) {

            throw new AILogAnalyserClientException(
                    "Unexpected error while communicating with AILogAnalyser server.",
                    ex
            );
        }
    }
}
