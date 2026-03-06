package com.uplus.crm.domain.elasticsearch.controller;

import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import com.uplus.crm.domain.elasticsearch.service.ConsultSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/elasticsearch/consult")
@RequiredArgsConstructor
public class ConsultSearchControllerTest {

    private final ConsultSearchService consultSearchService;

    /**
     * 테스트 데이터 생성
     * POST /api/elasticsearch/consult/test-data
     */
    @PostMapping("/test-data")
    public ResponseEntity<String> createTestData() {
        ConsultDoc doc = ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("갤럭시 S24 울트라 미납금 번호이동 제한")
                .iamAction("미납 납부 후 번호이동 가능 안내")
                .content("고객이 갤폰 미납금 있어서 번이 못 한다고 함")
                .allText("갤럭시 S24 울트라 미납금 번호이동 제한 미납 납부 후 가능 안내")
                .customerName("김유플")
                .customerId("C001")
                .sentiment("NEGATIVE")
                .riskScore(75)
                .priority("HIGH")
                .phone("010-1234-5678")
                .createdAt(LocalDateTime.now())
                .build();

        consultSearchService.saveConsultation(doc);
        return ResponseEntity.ok("테스트 데이터 저장 완료!");
    }

    /**
     * 통합 키워드 검색 (동의어 사전 적용)
     * - 갤폰 검색 → 갤럭시 포함 문서 반환
     * - 번이 검색 → 번호이동 포함 문서 반환
     * - 오타 허용 (fuzziness AUTO)
     * GET /api/elasticsearch/consult/search?keyword=갤폰&page=0&size=20
     */
    @GetMapping("/search")
    public ResponseEntity<List<ConsultDoc>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(consultSearchService.searchByKeyword(keyword, page, size));
    }

    /**
     * 고위험 상담 검색 (riskScore ≥ threshold 또는 위험 키워드)
     * GET /api/elasticsearch/consult/search/high-risk?keyword=해지&threshold=70
     */
    @GetMapping("/search/high-risk")
    public ResponseEntity<List<ConsultDoc>> searchHighRisk(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "70") int threshold) {
        return ResponseEntity.ok(consultSearchService.searchHighRisk(keyword, threshold));
    }

    /**
     * 감정 분류 + 키워드 복합 검색
     * GET /api/elasticsearch/consult/search/sentiment?sentiment=NEGATIVE&keyword=해지
     */
    @GetMapping("/search/sentiment")
    public ResponseEntity<List<ConsultDoc>> searchBySentiment(
            @RequestParam String sentiment,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                consultSearchService.searchBySentimentAndKeyword(sentiment, keyword, page, size));
    }

    /**
     * 날짜 범위 + 키워드 검색
     * GET /api/elasticsearch/consult/search/date?keyword=미납&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
     */
    @GetMapping("/search/date")
    public ResponseEntity<List<ConsultDoc>> searchByDate(
            @RequestParam(required = false) String keyword,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                consultSearchService.searchByDateRangeAndKeyword(keyword, from, to, page, size));
    }

    /**
     * 우선순위 필터 검색
     * GET /api/elasticsearch/consult/search/priority?priority=URGENT&keyword=해지
     */
    @GetMapping("/search/priority")
    public ResponseEntity<List<ConsultDoc>> searchByPriority(
            @RequestParam String priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                consultSearchService.searchByPriority(priority, keyword, page, size));
    }

    /**
     * 고객명 검색
     * GET /api/elasticsearch/consult/search/customer?name=김유플
     */
    @GetMapping("/search/customer")
    public ResponseEntity<List<ConsultDoc>> searchByCustomerName(@RequestParam String name) {
        return ResponseEntity.ok(consultSearchService.searchByCustomerName(name));
    }

    /**
     * 고객 ID로 상담 이력 조회 (최신순)
     * GET /api/elasticsearch/consult/customer/C001
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ConsultDoc>> getByCustomerId(@PathVariable String customerId) {
        return ResponseEntity.ok(consultSearchService.findByCustomerId(customerId));
    }
}
