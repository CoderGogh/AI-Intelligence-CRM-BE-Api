package com.uplus.crm.domain.account.service;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.uplus.crm.domain.account.dto.request.AdminEmployeeUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.AdminEmployeeUpdateResponseDto;
import com.uplus.crm.domain.account.entity.Department;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.entity.JobRole;
import com.uplus.crm.domain.account.repository.mysql.DepartmentRepository;
import com.uplus.crm.domain.account.repository.mysql.EmployeeDetailRepository;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.account.repository.mysql.JobRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeDetailRepository employeeDetailRepository;
    private final DepartmentRepository departmentRepository;
    private final JobRoleRepository jobRoleRepository;

    @Transactional
    public AdminEmployeeUpdateResponseDto updateEmployee(Integer empId, AdminEmployeeUpdateRequestDto req) {

        // 1) 직원 존재 확인 (404)
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 직원 없음"));

        // 2) 이메일 중복 체크 (409)
        if (employeeRepository.existsByEmailAndEmpIdNot(req.getEmail(), empId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이메일 중복");
        }

        // 3) 부서/역할 존재 확인 (보통 400 또는 404 - 여기선 400 처리)
        Department dept = departmentRepository.findById(req.getDeptId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "부서 ID 오류"));

        JobRole role = jobRoleRepository.findById(req.getJobRoleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "역할 ID 오류"));

        // 4) 문자열 날짜 파싱 (nullable)
        LocalDate birth = parseLocalDateOrNull(req.getBirth());
        LocalDate joinedAt = parseLocalDateOrNull(req.getJoinedAt());

        // 5) Employee 업데이트 (name/email/phone/birth/gender)
        employee.updateAccountInfo(
                req.getName(),
                req.getEmail(),
                req.getPhone(),
                birth,
                req.getGender()
        );

        // 6) EmployeeDetail 조회/없으면 생성 후 업데이트 (dept/role/joinedAt/updatedAt)
        EmployeeDetail detail = employeeDetailRepository.findById(empId)
                .orElseGet(() -> EmployeeDetail.builder()
                        .empId(empId)
                        .employee(employee)
                        .department(dept)
                        .jobRole(role)
                        .joinedAt(joinedAt)
                        .build()
                );

        // 기존 detail이면 업데이트
        detail.updateDetail(dept, role, joinedAt);

        // 7) 저장 (Employee는 더티체킹으로 업데이트되지만 명시적으로 저장해도 OK)
        employeeDetailRepository.save(detail);

        // 8) 응답 DTO
        return AdminEmployeeUpdateResponseDto.builder()
                .empId(employee.getEmpId())
                .name(employee.getName())
                .email(employee.getEmail())
                .deptName(detail.getDepartment().getDeptName())
                .roleName(detail.getJobRole().getRoleName())
                .updatedAt(detail.getUpdatedAt())
                .build();
    }

    private LocalDate parseLocalDateOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDate.parse(value); // "yyyy-MM-dd" 형식 전제 (명세서 예시 그대로)
    }
}