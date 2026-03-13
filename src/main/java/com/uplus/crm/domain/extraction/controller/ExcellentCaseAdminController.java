package com.uplus.crm.domain.extraction.controller;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uplus.crm.domain.extraction.dto.request.ExcellentCaseSearchRequest;
import com.uplus.crm.domain.extraction.dto.response.EvaluationListResponse;
import com.uplus.crm.domain.extraction.service.ExcellentCaseAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/excellent-cases")
@RequiredArgsConstructor
public class ExcellentCaseAdminController {
    private final ExcellentCaseAdminService adminService;

    @GetMapping("/candidates")
    public ResponseEntity<Page<EvaluationListResponse>> getCandidates(
            ExcellentCaseSearchRequest searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(adminService.getCandidatePage(searchRequest, page, size));
    }
}