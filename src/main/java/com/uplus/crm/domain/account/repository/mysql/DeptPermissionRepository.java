package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.DeptPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DeptPermissionRepository extends JpaRepository<DeptPermission, Integer> {

    @Query("SELECT dp FROM DeptPermission dp " +
           "JOIN FETCH dp.permission p " +
           "WHERE dp.department.deptId = :deptId " +
           "AND dp.isDeleted = false")
    List<DeptPermission> findByDeptIdWithPermission(@Param("deptId") Integer deptId);

    @Query("SELECT p.permCode FROM DeptPermission dp " +
           "JOIN dp.permission p " +
           "WHERE dp.department.deptId = :deptId AND dp.isDeleted = false")
    List<String> findPermCodesByDeptId(@Param("deptId") Integer deptId);
}
