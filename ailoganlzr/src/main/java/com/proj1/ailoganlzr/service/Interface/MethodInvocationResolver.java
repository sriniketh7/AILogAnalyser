package com.proj1.ailoganlzr.service.Interface;


import com.proj1.ailoganlzr.DTO.MethodInvocationInfo;
import com.proj1.ailoganlzr.DTO.ParsedClassContext;
import com.proj1.ailoganlzr.DTO.ResolvedMethodInvocation;

import java.util.List;

public interface MethodInvocationResolver {

    List<ResolvedMethodInvocation> resolve(
            List<MethodInvocationInfo> invocations,
            ParsedClassContext context
    );

}
