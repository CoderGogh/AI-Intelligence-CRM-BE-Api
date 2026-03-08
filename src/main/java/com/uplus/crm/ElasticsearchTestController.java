package com.uplus.crm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Tag(name = "① ES 셋업")
@RestController
@RequiredArgsConstructor
@RequestMapping("/es-test")
public class ElasticsearchTestController {

  private final RestClient restClient;
  private final ElasticsearchIndexInitializer indexInitializer;

  @Operation(
      tags = {"① ES 셋업"},
      summary = "인덱스 재생성 (사전 변경 시에만)",
      description = """
          기존 consult-index를 삭제하고 최신 분석기 설정으로 재생성합니다.
          **사전 파일(analysis_synonyms.txt 등)을 수정한 후에만 호출하세요.**
          평소 앱 재시작 시에는 호출할 필요가 없습니다.

          실행 후 반드시 데이터를 재적재해야 합니다.
          - 실제 데이터: POST /admin/es/sync
          - 더미 데이터: POST /elasticsearch/consult/test-data

          아래 증상이 발생하면 호출하세요:
          - 'failed to find analyzer [korean_index_analyzer]' 오류
          - 검색 결과가 모두 빈 배열
          """
  )
  @PostMapping("/recreate-index")
  public ResponseEntity<String> recreateIndex() {
    try {
      String result = indexInitializer.forceRecreate();
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("인덱스 재생성 실패: " + e.getMessage());
    }
  }

  @Operation(
      tags = {"③ ES 분석"},
      summary = "사전 적용 확인 (토크나이징 결과)",
      description = """
          입력 텍스트가 분석기에서 어떤 토큰으로 변환되는지 확인합니다.
          사전 파일 수정 후 정상 반영 여부를 검증할 때 사용합니다.

          **analyzer 선택**
          - `korean_index_analyzer` (기본): 검색용 동의어 사전 적용
          - `korean_analysis_index_analyzer`: 응대품질 분석용 사전 적용

          **동의어 확인 예시** (검색용)
          - 갤폰 → [갤럭시], 번이 → [번호이동], 넷플 → [넷플릭스], 기변 → [기기변경]

          **분석용 사전 확인 예시**
          - 잠시기다려주세요 → [대기안내]
          - 충분히이해합니다 → [공감응대]
          - 친절하게해주셨다 → [친절응대]
          """
  )
  @GetMapping("/analyze")
  public ResponseEntity<String> analyze(
      @Parameter(description = "분석할 텍스트 (구어체·축약어 입력 가능)", example = "갤폰 번이 해지")
      @RequestParam String text,
      @Parameter(description = "사용할 분석기 이름 (korean_index_analyzer / korean_search_analyzer)", example = "korean_index_analyzer")
      @RequestParam(defaultValue = "korean_index_analyzer") String analyzer) {

    try {
      String body = """
          {
            "analyzer": "%s",
            "text": "%s"
          }
          """.formatted(analyzer, text);

      Request request = new Request("POST", "/consult-index/_analyze");
      request.setJsonEntity(body);

      Response response = restClient.performRequest(request);

      String result = new BufferedReader(
          new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8)
      ).lines().collect(Collectors.joining());

      return ResponseEntity.ok(result);

    } catch (ResponseException e) {
      return ResponseEntity.status(e.getResponse().getStatusLine().getStatusCode())
          .body("ES 오류: " + e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("분석 실패: " + e.getMessage());
    }
  }
}