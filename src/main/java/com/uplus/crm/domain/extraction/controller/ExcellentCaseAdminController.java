package com.uplus.crm.domain.extraction.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uplus.crm.domain.extraction.dto.request.ExcellentCaseSearchRequest;
import com.uplus.crm.domain.extraction.dto.response.EvaluationListResponse;
import com.uplus.crm.domain.extraction.service.ExcellentCaseAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Excellent Case", description = "관리자용 우수 사례 후보군 관리 API")
@RestController
@RequestMapping("/admin/excellent-cases")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") 
public class ExcellentCaseAdminController {

    private final ExcellentCaseAdminService adminService;

    @Operation(summary = "우수 사례 후보군 리스트 조회", 
               description = "상태값(PENDING, SELECTED 등)과 정렬 기준을 사용하여 후보 목록을 페이징 조회합니다.")
    @GetMapping("/candidates")
    public ResponseEntity<Page<EvaluationListResponse>> getCandidates(
            @ParameterObject ExcellentCaseSearchRequest searchRequest, 
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "한 페이지당 개수", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(adminService.getCandidatePage(searchRequest, page, size));
    }
}