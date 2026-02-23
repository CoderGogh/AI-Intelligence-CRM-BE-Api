package com.uplus.crm.domain.account.controller;

import com.uplus.crm.domain.account.dto.request.AdminEmployeeUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.AdminEmployeeUpdateResponseDto;
import com.uplus.crm.domain.account.service.AdminEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/employees")
public class AdminEmployeeController {

    private final AdminEmployeeService adminEmployeeService;

    @PutMapping("/{id}")
    public ResponseEntity<AdminEmployeeUpdateResponseDto> updateEmployee(
            @PathVariable("id") Integer id,
            @RequestBody AdminEmployeeUpdateRequestDto request
    ) {
        AdminEmployeeUpdateResponseDto response = adminEmployeeService.updateEmployee(id, request);
        return ResponseEntity.ok(response);
    }
}