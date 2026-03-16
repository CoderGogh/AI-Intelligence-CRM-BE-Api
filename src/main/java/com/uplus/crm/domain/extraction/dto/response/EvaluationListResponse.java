package com.uplus.crm.domain.extraction.dto.response;

import com.uplus.crm.domain.extraction.entity.SelectionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record EvaluationListResponse(
    @Schema(description = "상담 ID", example = "16")
    Long consultId,

    @Schema(description = "소분류 카테고리명", example = "상품 업그레이드 제안")
    String categoryName,

    @Schema(description = "상담사 이름", example = "승혁")
    String counselorName,

    @Schema(description = "AI 채점 점수", example = "95")
    Integer score,

    @Schema(description = "상담 요약 제목", example = "U+ 번호이동 방어 혜택 안내 및 재약정 성공")
    String title,

    @Schema(description = "현재 선정 상태")
    SelectionStatus selectionStatus,

    @Schema(description = "분석 생성 일시")
    LocalDateTime createdAt
) {}