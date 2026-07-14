package com.proj1.ailoganlzr.mapper;

import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResponseDto;
import com.proj1.ailoganlzr.DTO.Response.AnalysisResultResponseDto;
import com.proj1.ailoganlzr.Model.AnalysisRequest;
import com.proj1.ailoganlzr.Model.AnalysisResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalysisMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "requestHash", ignore = true)
    @Mapping(target = "analysisType", ignore = true)
    @Mapping(target = "analysisResult", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AnalysisRequest toEntity(AnalysisRequestDto dto);

    @Mapping(source = "analysisRequest.id", target = "requestId")
    @Mapping(source = "analysisRequest.title", target = "title")
    @Mapping(source = "analysisRequest.status", target = "status")
    AnalysisResultResponseDto toResponseDto(AnalysisResult entity);

}
