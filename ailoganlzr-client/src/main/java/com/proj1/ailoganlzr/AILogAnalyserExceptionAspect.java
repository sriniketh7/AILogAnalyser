package com.proj1.ailoganlzr;

import com.proj1.ailoganlzr.DTO.Response.AnalysisResultResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
public class AILogAnalyserExceptionAspect {

    private final AILogAnalyserClient aiLogAnalyserClient;

    public AILogAnalyserExceptionAspect(
            AILogAnalyserClient aiLogAnalyserClient) {
        this.aiLogAnalyserClient = aiLogAnalyserClient;
    }

    @Around(
            "@within(org.springframework.web.bind.annotation.RestController) || " +
                    "@within(org.springframework.stereotype.Controller)"
    )
    public Object analyseUnhandledExceptions(
            ProceedingJoinPoint joinPoint) throws Throwable {


        try {
            Object result = joinPoint.proceed();


            return result;


        } catch (Throwable ex) {
            try {
                // Send exception and receive AI analysis
                AnalysisResultResponseDto analysis =
                        aiLogAnalyserClient.sendAnalysis(ex);

                // Display the useful result in host application's terminal
                log.error("""
                        
                        ========== AILogAnalyser Result ==========
                        Exception : {}
                        Summary   : {}
                        Root Cause: {}
                        Solution  : {}
                        Confidence: {}%
                        ==========================================
                        """,
                        ex.getClass().getSimpleName(),
                        analysis.getSummary(),
                        analysis.getRootCause(),
                        analysis.getSolution(),
                        analysis.getConfidence()
                );

            } catch (Exception analysisException) {

                // Failure of AILogAnalyser should not affect
                // the host application's exception handling
                log.error(
                        "AILogAnalyser failed to analyse exception: {}",
                        analysisException.getMessage()
                );
            }


            throw ex;
        }
    }
}