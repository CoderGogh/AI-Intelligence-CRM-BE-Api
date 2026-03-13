package com.uplus.crm.domain.extraction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.domain.extraction.dto.request.ExcellentCaseSearchRequest;
import com.uplus.crm.domain.extraction.dto.response.EvaluationListResponse;
import com.uplus.crm.domain.extraction.repository.ConsultationEvaluationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcellentCaseAdminService {
    private final ConsultationEvaluationRepository evaluationRepository;

    @Transactional(readOnly = true)
    public Page<EvaluationListResponse> getCandidatePage(ExcellentCaseSearchRequest request, int page, int size) {
        
        // 1. 상태값(status) 처리: 입력 없거나 "string"이면 null 전달 
        String status = request.status();
        if (status == null || status.isBlank() || "string".equalsIgnoreCase(status) || "ALL".equalsIgnoreCase(status)) {
            status = null;
        }

        // 2. 정렬 기준(sortBy) 처리: 입력 없거나 "string"이면 기본값 "score"
        String sortBy = request.sortBy();
        if (sortBy == null || sortBy.isBlank() || "string".equalsIgnoreCase(sortBy)) {
            sortBy = "score";
        }

        // 3. 정렬 방향(direction) 처리: 기본값 DESC
        Sort.Direction direction = Sort.Direction.DESC;
        String dirInput = request.direction();
        
        if (dirInput != null && !dirInput.isBlank() && !"string".equalsIgnoreCase(dirInput)) {
            try {
                direction = Sort.Direction.fromString(dirInput);
            } catch (IllegalArgumentException e) {
                // "abc" 같이 엉뚱한 값이 들어오면 기본값 DESC 유지
                direction = Sort.Direction.DESC;
            }
        }

        // 4. 최종 Pageable 조립 및 호출
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return evaluationRepository.findCandidatePage(status, pageable);
    }
}