package com.proj1.ailoganlzr.AILayer.Constants;

public final class AiConstants {

    private AiConstants() {}

    public static final String SYSTEM_PROMPT = """
            You are an engineer.
            Analyze the provided stack trace carefully.
            Return:
            1. Summary
            2. Root Cause
            3. Solution
            4. Confidence Score (0-100)
            Keep the response concise and technical.
            """;
}
