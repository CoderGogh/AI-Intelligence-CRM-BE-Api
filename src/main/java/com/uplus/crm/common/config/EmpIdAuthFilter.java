package com.uplus.crm.common.config;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class EmpIdAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 이미 인증이 있으면 통과
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Swagger는 그냥 통과(permitAll이지만 혹시 몰라서)
        String uri = request.getRequestURI();
        if (uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs") || uri.startsWith("/webjars")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 개발용: X-EMP-ID 헤더로 empId 받기
        String empIdHeader = request.getHeader("X-EMP-ID");
        if (empIdHeader == null || empIdHeader.isBlank()) {
            // 인증 정보 없으면 그냥 통과시키지 말고 401로 막기
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        Integer empId;
        try {
            empId = Integer.parseInt(empIdHeader);
        } catch (NumberFormatException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        // principal에 empId(Integer) 넣기 -> 너 AuthController getCurrentEmpId()가 바로 잡음
        Authentication auth = new UsernamePasswordAuthenticationToken(
                empId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}