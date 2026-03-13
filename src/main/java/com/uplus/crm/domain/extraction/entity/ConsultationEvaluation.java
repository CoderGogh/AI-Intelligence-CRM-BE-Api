package com.uplus.crm.domain.extraction.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "consultation_evaluations", indexes = {
    @Index(name = "idx_eval_candidate_status", columnList = "isCandidate, selectionStatus")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // 생성일 자동 기록을 위해 필요
@ToString
public class ConsultationEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long evaluationId;

    @Column(nullable = false)
    private Long consultId;

    @Column(nullable = false)
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String evaluationReason;

    @Column(nullable = false)
    private boolean isCandidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SelectionStatus selectionStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ConsultationEvaluation(Long consultId, Integer score, String evaluationReason, boolean isCandidate) {
        this.consultId = consultId;
        this.score = score;
        this.evaluationReason = evaluationReason;
        this.isCandidate = isCandidate;
        this.selectionStatus = SelectionStatus.PENDING; // 기본값은 항상 '검토 대기'
    }

    // 관리자가 우수 사례 선정/제외를 결정할 때 호출할 상태 변경 메서드
    public void updateSelectionStatus(SelectionStatus status) {
        this.selectionStatus = status;
    }
}