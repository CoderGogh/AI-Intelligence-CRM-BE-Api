package com.uplus.crm.domain.account.controller;

import com.uplus.crm.domain.account.dto.request.AdminEmployeeUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.AdminEmployeeUpdateResponseDto;
import com.uplus.crm.domain.account.service.AdminEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/employees")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmployeeController {

    private final AdminEmployeeService adminEmployeeService;

    @PutMapping("/{id}")
    public ResponseEntity<AdminEmployeeUpdateResponseDto> updateEmployee(
            @PathVariable("id") Integer id,
            @RequestBody AdminEmployeeUpdateRequestDto request
    ) {
        return ResponseEntity.ok(adminEmployeeService.updateEmployee(id, request));
    }
}
