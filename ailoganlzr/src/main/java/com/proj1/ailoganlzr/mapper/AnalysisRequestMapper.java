package com.proj1.ailoganlzr.mapper;

import com.proj1.ailoganlzr.DTO.AnalysisRequestDto;
import com.proj1.ailoganlzr.DTO.AnalysisResponseDto;
import com.proj1.ailoganlzr.Model.AnalysisRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnalysisRequestMapper {

    AnalysisRequest toEntity(AnalysisRequestDto dto);

    AnalysisResponseDto toDto(AnalysisRequest entity);

}
