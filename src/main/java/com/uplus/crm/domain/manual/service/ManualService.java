package com.uplus.crm.domain.manual.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import com.uplus.crm.domain.consultation.repository.ConsultationCategoryRepository;
import com.uplus.crm.domain.manual.dto.request.ManualRequest;
import com.uplus.crm.domain.manual.dto.response.ManualResponse;
import com.uplus.crm.domain.manual.entity.Manual;
import com.uplus.crm.domain.manual.repository.ManualRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManualService {
    private final ManualRepository manualRepository;
    private final ConsultationCategoryRepository policyRepository;
    private final EmployeeRepository employeeRepository;

    /** 1. 작성 (Create): 신규 등록 시 기존 매뉴얼은 자동 비활성화 */
    @Transactional
    public void createManual(ManualRequest request, Integer empId) {
        ConsultationCategoryPolicy policy = policyRepository.findById(request.categoryCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_POLICY_NOT_FOUND));

        Employee employee = employeeRepository.findById(empId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // 기존에 켜져있던 매뉴얼은 끈다
        manualRepository.findByCategoryPolicy_CategoryCodeAndIsActiveTrue(request.categoryCode())
            .ifPresent(old -> old.setIsActive(false));

        Manual manual = new Manual();
        manual.setCategoryPolicy(policy);
        manual.setEmployee(employee);
        manual.setTitle(request.title());
        manual.setContent(request.content());
        manual.setIsActive(true);

        manualRepository.save(manual);
    }

    /** 2. 수정 (Update): 특정 매뉴얼의 제목과 내용을 변경 */
    @Transactional
    public void updateManual(Integer id, ManualRequest request) {
        Manual manual = manualRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MANUAL_NOT_FOUND));
        
        manual.setTitle(request.title());
        manual.setContent(request.content());
    }

    /** 3. 조회 (Read): 카테고리별 목록 조회 */
    public List<ManualResponse> getHistory(String categoryCode) {
        return manualRepository.findAllByCategoryPolicy_CategoryCodeOrderByCreatedAtDesc(categoryCode)
            .stream()
            .map(ManualResponse::from)
            .toList();
    }

    /** 4. 비활성화 (Deactivate): 수동으로 사용 중지 */
    @Transactional
    public void deactivateManual(Integer id) {
        Manual manual = manualRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MANUAL_NOT_FOUND));
        manual.setIsActive(false);
    }

    /** 5. 활성화 (Activate): 과거 매뉴얼을 다시 사용 상태로 변경 */
    @Transactional
    public void activateManual(Integer id) {
        Manual manual = manualRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MANUAL_NOT_FOUND));

        // 해당 카테고리에 이미 활성화된 다른 매뉴얼이 있다면 먼저 끈다
        manualRepository.findByCategoryPolicy_CategoryCodeAndIsActiveTrue(manual.getCategoryPolicy().getCategoryCode())
            .ifPresent(old -> old.setIsActive(false));

        manual.setIsActive(true);
    }
}