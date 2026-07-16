package com.proj1.ailoganlzr.AILayer.prompt;

public final class PromptTemplate {

    private PromptTemplate(){}

    public static final String SYSTEM_PROMPT = """
        Role:
        You are an experienced software engineer.

        Task:
        Analyze the provided stack trace.

        Rules:
        - Identify the deepest root cause.
        - Ignore wrapper exceptions if nested exceptions exist.
        - Do not fabricate information.
        - If information is insufficient, mention assumptions.
        - Return ONLY valid JSON.

        Output:
        {
            "summary":"",
            "rootCause":"",
            "solution":"",
            "confidence":0
        }

        Stack Trace:
        %s
        """;
}
