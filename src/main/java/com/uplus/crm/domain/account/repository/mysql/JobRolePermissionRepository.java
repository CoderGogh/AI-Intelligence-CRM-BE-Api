package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.JobRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface JobRolePermissionRepository extends JpaRepository<JobRolePermission, Integer> {
    
    // 1. 상세 조회를 위해 Permission 엔티티까지 한 번에 가져오기 (Fetch Join)
    @Query("SELECT jrp FROM JobRolePermission jrp " +
           "JOIN FETCH jrp.permission " +
           "WHERE jrp.jobRole.jobRoleId = :jobRoleId AND jrp.isDeleted = false")
    List<JobRolePermission> findByJobRoleIdWithPermission(@Param("jobRoleId") Integer jobRoleId);

    // 2. 내 정보 조회를 위해 권한 코드(String)만 싹 가져오기
    @Query("SELECT jrp.permission.permCode FROM JobRolePermission jrp " +
           "WHERE jrp.jobRole.jobRoleId = :jobRoleId AND jrp.isDeleted = false")
    List<String> findPermCodesByJobRoleId(@Param("jobRoleId") Integer jobRoleId);
}