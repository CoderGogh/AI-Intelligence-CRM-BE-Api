package com.uplus.crm.domain.extraction.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "retention_analysis")
@Getter
@NoArgsConstructor
public class RetentionAnalysis {
    @Id
    private Long consultId; // 상담 ID를 PK로 사용

    @Column(columnDefinition = "TEXT")
    private String rawSummary; // 요약 내용
}