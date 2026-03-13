package com.uplus.crm.domain.extraction.controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; 
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.uplus.crm.domain.extraction.dto.EvaluationListResponse;
import com.uplus.crm.domain.extraction.service.ExcellentCaseAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/excellent-cases")
@RequiredArgsConstructor
public class ExcellentCaseAdminController {
    private final ExcellentCaseAdminService adminService;

    @GetMapping("/candidates")
    public ResponseEntity<Page<EvaluationListResponse>> getCandidates(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "score", direction = Sort.Direction.DESC) Pageable pageable) {
        
        return ResponseEntity.ok(adminService.getCandidatePage(status, pageable));
    }
}