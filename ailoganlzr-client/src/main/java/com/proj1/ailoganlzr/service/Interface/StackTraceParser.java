package com.proj1.ailoganlzr.service.Interface;

import com.proj1.ailoganlzr.DTO.StackFrame;

import java.util.List;

public interface StackTraceParser {

    List<StackFrame> parseStackTrace(String stackTrace);
}
