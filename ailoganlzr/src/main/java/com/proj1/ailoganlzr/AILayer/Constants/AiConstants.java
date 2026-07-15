package com.proj1.ailoganlzr.AILayer.Constants;

public final class AiConstants {

    private AiConstants() {}

    public static final String SYSTEM_PROMPT = """
            You are an engineer.
            Analyze the provided stack trace carefully.
            Return Only Valid JSON.
            {
            "summary": "Brief description of the issue",
            "rootCause": "Identified root cause",
            "solution": "Proposed solution",
            "confidence": 0-100
            }
            Stack Trace:
            <rawLog>
            Keep the response concise and technical.
            """;
}
