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

    AnalysisRequest toEntity(AnalysisRequestDto dto);

    @Mapping(source = "analysisRequest.id", target = "requestId")
    @Mapping(source = "analysisRequest.title", target = "title")
    @Mapping(source = "analysisRequest.status", target = "status")
    AnalysisResultResponseDto toResponseDto(AnalysisResult entity);

}
