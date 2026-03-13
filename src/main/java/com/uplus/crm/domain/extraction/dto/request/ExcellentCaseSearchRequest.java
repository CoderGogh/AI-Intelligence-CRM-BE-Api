package com.uplus.crm.domain.extraction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ExcellentCaseSearchRequest(
    @Schema(description = "선정 상태 (PENDING, SELECTED, REJECTED, ALL)", 
            allowableValues = {"PENDING", "SELECTED", "REJECTED", "ALL"},
            defaultValue = "ALL", 
            example = "ALL")
    String status,

    @Schema(description = "정렬 기준 필드 (null/빈값일 경우 최신순, 'score' 입력 시 점수순)", 
            defaultValue = "", 
            example = "") 
    String sortBy, 

    @Schema(description = "정렬 방향 (asc, desc)", 
            allowableValues = {"asc", "desc"},
            defaultValue = "desc", 
            example = "desc")
    String direction 
) {}