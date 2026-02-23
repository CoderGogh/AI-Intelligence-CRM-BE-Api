package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    // 수정 시 본인(empId) 제외하고 이메일 중복 체크
    boolean existsByEmailAndEmpIdNot(String email, Integer empId);
}
