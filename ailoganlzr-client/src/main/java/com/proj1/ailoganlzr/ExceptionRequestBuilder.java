package com.proj1.ailoganlzr;

import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.CodeSnippet;
import com.proj1.ailoganlzr.service.Interface.CodeSnippetExtractionService;
import lombok.extern.slf4j.Slf4j;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Slf4j
public class ExceptionRequestBuilder {
    private final CodeSnippetExtractionService codeSnippetExtractionService;

    public ExceptionRequestBuilder(CodeSnippetExtractionService codeSnippetExtractionService) {
        this.codeSnippetExtractionService = codeSnippetExtractionService;
    }

    public AnalysisRequestDto build(Throwable exception) {

        String rawLog = buildRawLog(exception);

        List<CodeSnippet> codeSnippets =
                codeSnippetExtractionService.extractSnippets(rawLog);

        log.info(
                "Extracted {} code snippets from host application",
                codeSnippets.size()
        );
        AnalysisRequestDto request = new AnalysisRequestDto();

        request.setTitle(buildTitle(exception));
        request.setRawLog(buildRawLog(exception));
        request.setCodeSnippets(codeSnippets);



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
