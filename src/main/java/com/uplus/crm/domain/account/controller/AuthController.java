package com.uplus.crm.domain.account.controller;

import com.uplus.crm.domain.account.dto.request.MyInfoUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.MyInfoUpdateResponseDto;
import com.uplus.crm.domain.account.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PutMapping("/me")
    public ResponseEntity<MyInfoUpdateResponseDto> updateMyInfo(@RequestBody MyInfoUpdateRequestDto req) {
        Integer empId = getCurrentEmpId();
        MyInfoUpdateResponseDto res = authService.updateMyInfo(empId, req);
        return ResponseEntity.ok(res);
    }

    private Integer getCurrentEmpId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "인증 실패"
            );
        }

        Object principal = auth.getPrincipal();

        // ✅ 케이스 1) principal이 Integer/String(empId)로 들어오는 경우
        if (principal instanceof Integer) return (Integer) principal;
        if (principal instanceof String s) {
            // 보통 "anonymousUser" 같은 값이 들어올 수 있음
            try { return Integer.parseInt(s); } catch (Exception ignored) {}
        }

        // ✅ 케이스 2) 커스텀 UserDetails에 getEmpId()가 있는 경우 (가장 흔함)
        // 예: CustomUserDetails implements UserDetails { Integer getEmpId(); }
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