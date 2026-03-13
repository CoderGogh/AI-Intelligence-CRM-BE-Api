package com.uplus.crm.domain.extraction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.domain.extraction.dto.EvaluationListResponse;
import com.uplus.crm.domain.extraction.repository.ConsultationEvaluationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcellentCaseAdminService {
    private final ConsultationEvaluationRepository evaluationRepository;

    @Transactional(readOnly = true)
    public Page<EvaluationListResponse> getCandidatePage(String status, Pageable pageable) {
        return evaluationRepository.findCandidatePage(status, pageable);
    }
}