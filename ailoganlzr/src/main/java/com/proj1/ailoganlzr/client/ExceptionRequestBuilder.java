package com.proj1.ailoganlzr.client;

import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;

@Component
public class ExceptionRequestBuilder {

    public AnalysisRequestDto build(Throwable exception) {

        AnalysisRequestDto request = new AnalysisRequestDto();

        request.setTitle(buildTitle(exception));
        request.setRawLog(buildRawLog(exception));

        return request;
    }

    private String buildTitle(Throwable exception) {

        return exception.getClass().getSimpleName();
    }

    private String buildRawLog(Throwable exception) {

        StringWriter stringWriter = new StringWriter();

        exception.printStackTrace(new PrintWriter(stringWriter));

        return stringWriter.toString();
    }
}
