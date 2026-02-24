package com.uplus.crm.domain.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uplus.crm.domain.account.dto.request.GoogleAuthRequestDto;
import com.uplus.crm.domain.account.dto.request.LoginRequestDto;
import com.uplus.crm.domain.account.dto.request.MyInfoUpdateRequestDto;
import com.uplus.crm.domain.account.dto.request.PasswordChangeRequestDto;
import com.uplus.crm.domain.account.dto.response.GoogleAuthResponseDto;
import com.uplus.crm.domain.account.dto.response.LoginResponseDto;
import com.uplus.crm.domain.account.dto.response.LogoutResponseDto;
import com.uplus.crm.domain.account.dto.response.MyInfoUpdateResponseDto;
import com.uplus.crm.domain.account.dto.response.PasswordChangeResponseDto;
import com.uplus.crm.domain.account.dto.response.TokenRefreshResponseDto;
import com.uplus.crm.domain.account.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ✅ (내 코드) PUT /auth/me — 내 정보 수정
    @Operation(summary = "내 정보 수정", description = "로그인한 사용자 본인의 정보 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PutMapping("/me")
    public ResponseEntity<MyInfoUpdateResponseDto> updateMyInfo(
            @RequestBody MyInfoUpdateRequestDto req
    ) {
        Integer empId = getCurrentEmpId();
        MyInfoUpdateResponseDto res = authService.updateMyInfo(empId, req);
        return ResponseEntity.ok(res);
    }

    // POST /auth/google
    @Operation(summary = "Google OAuth 로그인", description = "Google authorization code를 전달받아 로그인 처리")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Google 로그인 성공"),
            @ApiResponse(responseCode = "400", description = "authorization code 유효하지 않음"),
            @ApiResponse(responseCode = "401", description = "Google 인증 실패"),
            @ApiResponse(responseCode = "404", description = "연동된 계정 없음")
    })
    @PostMapping("/google")
    public ResponseEntity<GoogleAuthResponseDto> googleLogin(
            @Valid @RequestBody GoogleAuthRequestDto request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.googleLogin(request, response));
    }

    // POST /auth/login
    @Operation(summary = "일반 로그인", description = "계정 아이디와 비밀번호로 로그인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "파라미터 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    // POST /auth/logout
    @Operation(summary = "로그아웃", description = "HttpOnly Cookie의 Refresh Token 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "Access Token 유효하지 않음")
    })
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDto> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.logout(request, response));
    }

    // POST /auth/refresh
    @Operation(summary = "토큰 갱신", description = "HttpOnly Cookie의 Refresh Token으로 Access Token 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "Refresh Token 만료 또는 유효하지 않음")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDto> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.refresh(request, response));
    }

    // PUT /auth/me/password
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "새 비밀번호와 확인 비밀번호 불일치"),
            @ApiResponse(responseCode = "401", description = "현재 비밀번호 불일치")
    })
    @PutMapping("/me/password")
    public ResponseEntity<PasswordChangeResponseDto> changePassword(
            @AuthenticationPrincipal Integer empId,
            @Valid @RequestBody PasswordChangeRequestDto request
    ) {
        return ResponseEntity.ok(authService.changePassword(empId, request));
    }

    // ✅ (내 코드) empId 꺼내는 로직 유지
    private Integer getCurrentEmpId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "인증 실패"
            );
        }

        Object principal = auth.getPrincipal();

        // 케이스 1) principal이 Integer/String(empId)로 들어오는 경우
        if (principal instanceof Integer) return (Integer) principal;
        if (principal instanceof String s) {
            try { return Integer.parseInt(s); } catch (Exception ignored) {}
        }

        // 케이스 2) 커스텀 UserDetails에 getEmpId()가 있는 경우
        try {
            var method = principal.getClass().getMethod("getEmpId");
            Object v = method.invoke(principal);
            if (v instanceof Integer) return (Integer) v;
        } catch (Exception ignored) {}

        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "인증 실패"
        );
    }
}