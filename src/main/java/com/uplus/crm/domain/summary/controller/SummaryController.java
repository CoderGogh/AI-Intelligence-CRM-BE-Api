package com.uplus.crm.domain.summary.controller;

import com.uplus.crm.domain.summary.dto.request.SummarySearchRequest;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDetailResponse;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryListResponse;
import com.uplus.crm.domain.summary.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "상담요약", description = "AI 요약 처리가 완료된 상담 요약문 검색 및 조회")
@RestController
@RequestMapping("/api/summaries")
@RequiredArgsConstructor
public class SummaryController {

  private final SummaryService service;

  @Operation(
      summary = "IAM 검색 추천 키워드",
      description = """
          IAM 기반 검색(issue / action / memo) 및 통합 keyword 검색 입력창에서
          자동완성·추천검색어를 제공합니다.

          **데이터 소스 (MongoDB)**
          - `summary.keywords` : AI가 추출한 상담 키워드 (주 소스, 빈도 내림차순)
          - `iam.matchKeyword`  : 상담 중 실제 매칭된 키워드 (보조 소스)

          **파라미터**
          - `q`    : 입력 중인 prefix. 미입력 시 전체 인기 키워드 Top N 반환
          - `size` : 반환 개수 (기본 10, 최대 30)

          **적용 위치**
          - `keyword` 통합 검색 입력창
          - `iamIssue` / `iamAction` / `iamMemo` 각 입력창
          - 저장된 검색조건 재실행 전 조건 확인 화면
          """)
  @GetMapping("/suggest")
  public List<String> suggest(
      @Parameter(description = "검색어 prefix (미입력 시 인기 키워드 반환)", example = "해지")
      @RequestParam(required = false) String q,
      @Parameter(description = "반환 개수 (최대 30)", example = "10")
      @RequestParam(defaultValue = "10") int size) {

    return service.suggestKeywords(q, Math.min(size, 30));
  }

  @Operation(
      summary = "상담요약 목록 검색",
      description = """
          복합 조건으로 상담 요약문을 검색합니다.

          **기본 검색**
          - `keyword` : iam.issue / action / memo / summary.content / keywords 전체 OR 검색
          - `from` / `to` : 상담 기간 (yyyy-MM-dd)
          - `agentId` : 담당 상담사 ID
          - `agentName` : 담당 상담사 이름 (부분 일치)
          - `categoryCode` : 상담 카테고리 코드 (예: M_FEE_01)
          - `channel` : 상담 채널 (PHONE / CHAT)

          **IAM 기반 상세 검색**
          - `iamIssue` : 상담 키워드 (iam.issue 부분 일치)
          - `iamAction` : 상담 조치사항 (iam.action 부분 일치)
          - `iamMemo` : 상담 특이사항 (iam.memo 부분 일치)

          **고객 기반 상세 검색**
          - `customerName` : 고객 이름 (성 제외 이름만 입력 가능)
          - `customerPhone` : 고객 연락처 (부분 일치)
          - `customerType` : 고객 유형 (개인 / 법인)
          - `customerGrades` : 고객 등급 복수 선택 (VVIP, VIP, DIAMOND)

          **위험 유형 체크리스트** (OR 조건)
          - `riskTypes` : 폭언/욕설, 사기의심, 정책악용, 과도한 보상 요구, 반복민원, 해지위험, 피싱피해
          """)
  @GetMapping
  public Page<ConsultationSummaryListResponse> list(
      @ParameterObject @ModelAttribute SummarySearchRequest searchRequest,
      @ParameterObject @PageableDefault(
          size = 20,
          sort = "consultedAt",
          direction = Sort.Direction.DESC
      ) Pageable pageable) {

    return service.search(searchRequest, pageable);
  }

  @Operation(
      summary = "상담요약 상세 조회",
      description = "consultId로 상담 요약 상세 정보를 조회합니다. IAM, 해지 분석, 상품 계약 정보를 포함합니다.")
  @GetMapping("/{consultId}")
  public ConsultationSummaryDetailResponse detail(
      @Parameter(description = "상담 결과서 ID") @PathVariable Long consultId) {

    return service.getDetail(consultId);
  }
}