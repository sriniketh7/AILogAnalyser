package com.proj1.ailoganlzr.locator;

import com.proj1.ailoganlzr.DTO.StackFrame;

import java.nio.file.Path;
import java.util.Optional;

public interface SourceFileLocator {

    Optional<Path> locate(StackFrame frame);
}
