package com.uplus.crm.domain.elasticsearch.controller;

import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import com.uplus.crm.domain.elasticsearch.service.ConsultSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 응대품질·분석 전용 검색 컨트롤러.
 * analysis_synonyms.txt 기반 분석 사전을 사용하므로
 * 검색 사전(synonyms.txt)과는 별개로 동작한다.
 *
 * 검색 사전 기반 키워드 검색은 GET /summaries?keyword=... 를 사용할 것.
 */
@Tag(name = "③ ES 분석")
@RestController
@RequestMapping("/elasticsearch/consult")
@RequiredArgsConstructor
public class ConsultAnalysisController {

    private final ConsultSearchService consultSearchService;

    @Operation(
        summary = "[분석 Step 1] 응대품질 — 인삿말·마무리 누락 상담 조회",
        description = """
            분류: 분석된 데이터 검색
            인덱싱 시 실제 대화원문(consultation_raw_texts)의 상담사 발화를 분석하여
            자동 계산된 hasGreeting / hasFarewell 값을 기준으로 조회합니다.

            전제조건: POST /admin/es/sync 완료 (실제 대화원문 기반 분석)

            파라미터 조합
            - hasGreeting=false                   → 인사말 없이 시작한 상담
            - hasFarewell=false                   → 마무리 인사 없이 종료한 상담
            - hasGreeting=false&hasFarewell=false → 둘 다 없는 최우선 관리 대상
            - 파라미터 생략                       → 전체 상담 (riskScore 내림차순)

            감지 패턴 (고객 발화 제외, 상담사 발화만)
            - 인사말: 안녕하세요, 안녕하십니까, 반갑습니다 등
            - 마무리: 감사합니다, 수고하세요, 안녕히 계세요 등
            """
    )
    @GetMapping("/analysis/quality")
    public ResponseEntity<List<ConsultDoc>> analyzeQuality(
            @Parameter(description = "인사말 포함 여부 필터 (true/false, 생략 시 무조건)", example = "false")
            @RequestParam(required = false) Boolean hasGreeting,
            @Parameter(description = "마무리 인사 포함 여부 필터 (true/false, 생략 시 무조건)", example = "false")
            @RequestParam(required = false) Boolean hasFarewell,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                consultSearchService.searchByGreetingFlag(hasGreeting, hasFarewell, page, size));
    }

    @Operation(
        summary = "[분석 Step 2] 분석용 키워드 검색 — 응대 어근 제거 후 실질 내용 검색",
        description = """
            분류: 분석된 데이터 검색
            응대품질 분석 전용 분석기(korean_analysis_index_analyzer)로 인덱싱된
            allText.analysis 서브필드를 검색합니다.
            응대 어근이 제거된 토큰으로 검색하여 실질 상담 내용 패턴만 매칭됩니다.

            이 API가 사용하는 사전: analysis_synonyms.txt (분석 전용)
            검색 사전(synonyms.txt)은 적용되지 않습니다.

            [사전 적용 차이]
            - 꼼수, 번이, 갤폰 등 검색 동의어 → 이 API에서는 확장 안 됨
              → 검색 사전 기반 키워드는 GET /summaries?keyword=... 를 사용하세요.
            - 친절응대, 공감응대, 대기안내 등 응대품질 동의어 → 이 API에서 사용 가능

            활용 예시
            - keyword=미납         → 미납 관련 상담 패턴 탐지
            - keyword=해지         → 해지 의도 상담 군집 분석
            - keyword=친절응대     → 응대품질 우수 상담 검색
            - keyword=불만감정     → 고객 불만 감정 표현 상담 탐지
            """
    )
    @GetMapping("/analysis/keywords")
    public ResponseEntity<List<ConsultDoc>> analyzeKeywords(
            @Parameter(description = "분석할 키워드 (analysis_synonyms.txt 동의어 적용)", example = "미납")
            @RequestParam String keyword,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                consultSearchService.searchByAnalysisKeyword(keyword, page, size));
    }
}
