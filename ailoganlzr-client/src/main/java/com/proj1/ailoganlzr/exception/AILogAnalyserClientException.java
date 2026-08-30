package com.proj1.ailoganlzr.exception;

public class AILogAnalyserClientException extends RuntimeException {
    public AILogAnalyserClientException(String message) {
        super(message);
    }

    public AILogAnalyserClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
