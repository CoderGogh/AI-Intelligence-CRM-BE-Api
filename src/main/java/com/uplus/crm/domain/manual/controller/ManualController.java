package com.uplus.crm.domain.manual.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uplus.crm.common.security.CustomUserDetails;
import com.uplus.crm.domain.manual.dto.request.ManualRequest;
import com.uplus.crm.domain.manual.dto.response.ManualResponse;
import com.uplus.crm.domain.manual.service.ManualService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Manual Management", description = "관리자용 매뉴얼(채점 기준) 관리 API")
@RestController
@RequestMapping("/api/admin/manuals") // 경로 일관성 유지
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 클래스 전체에 관리자 권한 적용
public class ManualController {

    private final ManualService manualService;

    @Operation(summary = "매뉴얼 신규 작성/교체", description = "새로운 매뉴얼을 등록한다. 등록 시 기존 활성 매뉴얼은 자동으로 비활성화된다.")
    @PostMapping
    public ResponseEntity<Void> createManual(
            @RequestBody ManualRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails // 로그인한 유저 정보 주입 ⭐
    ) {
    	manualService.createManual(request, userDetails.getEmpId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "매뉴얼 내용 수정", description = "기존 매뉴얼의 제목과 내용을 수정한다.")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateManual(
            @PathVariable("id") Integer id,
            @RequestBody ManualRequest request
    ) {
        manualService.updateManual(id, request);
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
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateManual(@PathVariable("id") Integer id) {
        manualService.deactivateManual(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매뉴얼 수동 활성화", description = "비활성화된 매뉴얼을 현재 사용 중인 매뉴얼로 변경한다.")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateManual(@PathVariable("id") Integer id) {
        manualService.activateManual(id);
        return ResponseEntity.ok().build();
    }
}