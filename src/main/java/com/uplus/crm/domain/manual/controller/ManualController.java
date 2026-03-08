package com.uplus.crm.domain.manual.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.uplus.crm.common.security.CustomUserDetails;
import com.uplus.crm.domain.manual.dto.request.ManualRequest;
import com.uplus.crm.domain.manual.dto.response.ManualResponse;
import com.uplus.crm.domain.manual.service.ManualService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Manual Management", description = "관리자용 매뉴얼(채점 기준) 관리 API")
@RestController
@RequestMapping("/api/admin/manuals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ManualController {

    private final ManualService manualService;

    @Operation(summary = "매뉴얼 신규 작성/교체", description = "새로운 매뉴얼을 등록한다. 등록 시 기존 활성 매뉴얼은 자동으로 비활성화된다.")
    @PostMapping
    public ResponseEntity<Void> createManual(
            @RequestBody ManualRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        manualService.createManual(request, userDetails.getEmpId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "매뉴얼 내용 수정", description = "기존 매뉴얼의 제목과 내용을 수정한다.")
    @PutMapping("/{manualId}")
    public ResponseEntity<Void> updateManual(
            @PathVariable("manualId") Integer manualId, 
            @RequestBody ManualRequest request
    ) {
        manualService.updateManual(manualId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카테고리별 매뉴얼 이력 조회", description = "특정 카테고리에 등록된 모든 매뉴얼 리스트를 조회한다.")
    @GetMapping("/history/{categoryCode}")
    public ResponseEntity<List<ManualResponse>> getManualHistory(
            @PathVariable("categoryCode") String categoryCode
    ) {
        return ResponseEntity.ok(manualService.getHistory(categoryCode));
    }

    @Operation(summary = "매뉴얼 수동 비활성화", description = "활성화된 매뉴얼을 사용 중지 상태로 변경한다.")
    @PatchMapping("/{manualId}/deactivate") 
    public ResponseEntity<Void> deactivateManual(@PathVariable("manualId") Integer manualId) {
        manualService.deactivateManual(manualId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매뉴얼 수동 활성화", description = "비활성화된 매뉴얼을 현재 사용 중인 매뉴얼로 변경한다.")
    @PatchMapping("/{manualId}/activate")
    public ResponseEntity<Void> activateManual(@PathVariable("manualId") Integer manualId) {
        manualService.activateManual(manualId);
        return ResponseEntity.ok().build();
    }
}