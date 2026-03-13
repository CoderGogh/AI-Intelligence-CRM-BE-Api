package com.uplus.crm.domain.extraction.repository;

import com.uplus.crm.domain.extraction.dto.EvaluationListResponse;
import com.uplus.crm.domain.extraction.entity.ConsultationEvaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultationEvaluationRepository extends JpaRepository<ConsultationEvaluation, Long> {

    @Query("""
        SELECT new com.uplus.crm.domain.extraction.dto.EvaluationListResponse(
            e.consultId, 
            p.smallCategory, 
            emp.name, 
            e.score, 
            a.rawSummary, 
            e.selectionStatus, 
            e.createdAt
        )
        FROM ConsultationEvaluation e
        JOIN ConsultationResult r ON e.consultId = r.consultId
        JOIN Employee emp ON r.empId = emp.empId
        JOIN ConsultationCategoryPolicy p ON r.categoryCode = p.categoryCode
        JOIN RetentionAnalysis a ON e.consultId = a.consultId
        WHERE e.isCandidate = true
        AND (:status IS NULL OR CAST(e.selectionStatus AS string) = :status)
    """)
    Page<EvaluationListResponse> findCandidatePage(@Param("status") String status, Pageable pageable);
}