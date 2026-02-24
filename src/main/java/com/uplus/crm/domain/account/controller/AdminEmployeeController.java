package com.uplus.crm.domain.account.controller;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private final ObjectMapper objectMapper;

    @PutMapping("/{id}")
    public ResponseEntity<AdminEmployeeUpdateResponseDto> updateEmployee(
            @PathVariable("id") Integer id,
            @RequestBody JsonNode body
    ) {
        // 명세 키(dept_id/job_role_id/joined_at) -> DTO 키(deptId/jobRoleId/joinedAt) 보정
        if (body.isObject()) {
            ObjectNode obj = (ObjectNode) body;

            if (obj.has("dept_id") && !obj.has("deptId")) {
                obj.set("deptId", obj.get("dept_id"));
            }
            if (obj.has("job_role_id") && !obj.has("jobRoleId")) {
                obj.set("jobRoleId", obj.get("job_role_id"));
            }
            if (obj.has("joined_at") && !obj.has("joinedAt")) {
                obj.set("joinedAt", obj.get("joined_at"));
            }
        }

        AdminEmployeeUpdateRequestDto request =
                objectMapper.convertValue(body, AdminEmployeeUpdateRequestDto.class);

        AdminEmployeeUpdateResponseDto response = adminEmployeeService.updateEmployee(id, request);
        return ResponseEntity.ok(response);
    }
}