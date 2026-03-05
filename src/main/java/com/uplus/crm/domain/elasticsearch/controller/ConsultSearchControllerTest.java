package com.uplus.crm.domain.elasticsearch.controller;

import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import com.uplus.crm.domain.elasticsearch.service.ConsultSearchService;
import lombok.RequiredArgsConstructor;
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
     * 테스트 데이터 생성 API
     */
    @PostMapping("/test-data")
    public ResponseEntity<String> createTestData() {
        ConsultDoc doc = ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("갤럭시 S24 울트라 모델 미결제 문의")
                .content("고객님이 갤폰 미납금이 있어서 번이 제한 확인 요청함")
                .customerName("김유플")
                .createdAt(LocalDateTime.now())
                .build();

        consultSearchService.saveConsultation(doc);
        return ResponseEntity.ok("테스트 데이터 저장 완료!");
    }

    /**
     * 검색 API
     * GET /api/elasticsearch/consult/search?keyword=갤폰
     */
    @GetMapping("/search")
    public ResponseEntity<List<ConsultDoc>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(consultSearchService.searchByKeyword(keyword));
    }
}