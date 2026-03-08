package com.uplus.crm.domain.manual.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.uplus.crm.common.security.CustomUserDetails;
import com.uplus.crm.domain.manual.dto.request.ManualRequest;
import com.uplus.crm.domain.manual.dto.request.ManualUpdateRequest;
import com.uplus.crm.domain.manual.dto.response.ManualResponse;
import com.uplus.crm.domain.manual.service.ManualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Manual Management", description = "관리자용 매뉴얼(채점 기준) 관리 API")
@RestController
@RequestMapping("/api/admin/manuals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ManualController {

    private final ManualService manualService;

    @Operation(summary = "매뉴얼 신규 작성/교체")
    @PostMapping
    public ResponseEntity<Void> createManual(
            @Valid @RequestBody ManualRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        manualService.createManual(request, userDetails.getEmpId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "매뉴얼 내용 수정")
    @PutMapping("/{manualId}")
    public ResponseEntity<Void> updateManual(
            @PathVariable("manualId") Integer manualId, 
            @Valid @RequestBody ManualUpdateRequest request
    ) {
        manualService.updateManual(manualId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매뉴얼 이력 조회", description = "카테고리 코드가 있으면 필터링, 없으면 전체 조회")
    @GetMapping("/history") 
    public ResponseEntity<List<ManualResponse>> getManualHistory(
            @RequestParam(name = "categoryCode", required = false) String categoryCode 
    ) {
        return ResponseEntity.ok(manualService.getHistory(categoryCode));
    }

    @Operation(summary = "매뉴얼 수동 비활성화")
    @PatchMapping("/{manualId}/deactivate") 
    public ResponseEntity<Void> deactivateManual(@PathVariable("manualId") Integer manualId) {
        manualService.deactivateManual(manualId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매뉴얼 수동 활성화")
    @PatchMapping("/{manualId}/activate")
    public ResponseEntity<Void> activateManual(@PathVariable("manualId") Integer manualId) {
        manualService.activateManual(manualId);
        return ResponseEntity.ok().build();
    }
}