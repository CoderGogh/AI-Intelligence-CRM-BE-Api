package com.uplus.crm.domain.account.service;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.uplus.crm.domain.account.dto.request.MyInfoUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.MyInfoUpdateResponseDto;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;

    @Transactional
    public MyInfoUpdateResponseDto updateMyInfo(Integer empId, MyInfoUpdateRequestDto req) {

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 실패"));

        // 이메일 중복(409) - 본인 제외
        if (employeeRepository.existsByEmailAndEmpIdNot(req.getEmail(), empId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이메일 중복");
        }

        LocalDate birth = parseLocalDateOrNull(req.getBirth());

        employee.updateAccountInfo(
                req.getName(),
                req.getEmail(),
                req.getPhone(),
                birth,
                req.getGender()
        );

        // 응답
        return MyInfoUpdateResponseDto.builder()
                .empId(employee.getEmpId())
                .name(employee.getName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .birth(employee.getBirth() == null ? null : employee.getBirth().toString())
                .gender(employee.getGender())
                .build();
    }

    private LocalDate parseLocalDateOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDate.parse(value); // yyyy-MM-dd
    }
}