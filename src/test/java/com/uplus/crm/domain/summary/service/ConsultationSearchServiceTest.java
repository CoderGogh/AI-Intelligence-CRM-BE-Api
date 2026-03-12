package com.uplus.crm.domain.summary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.uplus.crm.domain.summary.document.es.ConsultSearchDocument;
import com.uplus.crm.domain.summary.dto.request.ConsultationSearchRequest;
import com.uplus.crm.domain.summary.repository.ConsultationMongoRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;

@ExtendWith(MockitoExtension.class)
class ConsultationSearchServiceTest {

  @InjectMocks
  private ConsultationSearchService consultationSearchService;

  @Mock
  private ElasticsearchOperations elasticsearchOperations;

  @Mock
  private ConsultationMongoRepository consultationMongoRepository;

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("search - 복합 검색 필터를 요청값에 맞게 생성한다")
  void search_buildsQueryFromRequest() {
    SearchHits<ConsultSearchDocument> emptyHits = (SearchHits<ConsultSearchDocument>) SearchHits.empty();
    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class))).willReturn(emptyHits);

    ConsultationSearchRequest request = new ConsultationSearchRequest();
    request.setKeyword("해지 방어");
    request.setIntent(Boolean.TRUE);
    request.setDefenseAttempted(Boolean.TRUE);
    request.setDefenseSuccess(Boolean.FALSE);
    request.setRiskType(List.of("R1", "R2"));
    request.setRiskLevel(List.of("L1", "L2"));
    request.setProductCode(List.of("P1", "P2"));
    request.setAgentName("홍길동");
    request.setCustomerName("김고객");
    request.setCustomerPhone("010-1234");

    consultationSearchService.search(request, PageRequest.of(0, 20));

    ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
    org.mockito.Mockito.verify(elasticsearchOperations)
        .search(queryCaptor.capture(), any(Class.class));

    String queryString = String.valueOf(queryCaptor.getValue().getQuery());

    assertThat(queryString).contains("intent");
    assertThat(queryString).contains("defenseAttempted");
    assertThat(queryString).contains("defenseSuccess");
    assertThat(queryString).contains("riskFlags.riskType");
    assertThat(queryString).contains("riskFlags.riskLevel");
    assertThat(queryString).contains("products");
    assertThat(queryString).contains("agentName");
    assertThat(queryString).contains("customerName");
    assertThat(queryString).contains("phone");
  }


  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("search - productCode 리스트는 keyword 필드까지 고려한 exact 필터를 생성한다")
  void search_productCodeList_usesKeywordAwareTermFilter() {
    SearchHits<ConsultSearchDocument> emptyHits = (SearchHits<ConsultSearchDocument>) SearchHits.empty();
    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class))).willReturn(emptyHits);

    ConsultationSearchRequest request = new ConsultationSearchRequest();
    request.setProductCode(List.of("MOB-NGT-07", "MOB-5G-ST", "TEL-HOME-UNL"));

    consultationSearchService.search(request, PageRequest.of(0, 20));

    ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
    org.mockito.Mockito.verify(elasticsearchOperations)
        .search(queryCaptor.capture(), any(Class.class));

    String queryString = String.valueOf(queryCaptor.getValue().getQuery());

    assertThat(queryString).contains("products");
    assertThat(queryString).contains("products.keyword");
    assertThat(queryString).contains("MOB-NGT-07");
    assertThat(queryString).contains("MOB-5G-ST");
    assertThat(queryString).contains("TEL-HOME-UNL");
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("search - consultedAt 날짜 범위를 ES LocalDateTime 문자열로 생성한다")
  void search_dateRange_usesEsDateTimeString() {
    SearchHits<ConsultSearchDocument> emptyHits = (SearchHits<ConsultSearchDocument>) SearchHits.empty();
    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class))).willReturn(emptyHits);

    ConsultationSearchRequest request = new ConsultationSearchRequest();
    request.setFromDate(LocalDate.of(2026, 3, 10));
    request.setToDate(LocalDate.of(2026, 3, 10));

    consultationSearchService.search(request, PageRequest.of(0, 20));

    ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
    org.mockito.Mockito.verify(elasticsearchOperations)
        .search(queryCaptor.capture(), any(Class.class));

    String queryString = String.valueOf(queryCaptor.getValue().getQuery());

    assertThat(queryString).contains("2026-03-10T00:00:00");
    assertThat(queryString).contains("2026-03-10T23:59:59");
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("search - 키워드가 없으면 consultedAt DESC 정렬을 적용한다")
  void search_withoutKeyword_appliesDefaultSort() {
    SearchHits<ConsultSearchDocument> emptyHits = (SearchHits<ConsultSearchDocument>) SearchHits.empty();
    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class))).willReturn(emptyHits);

    consultationSearchService.search(new ConsultationSearchRequest(), PageRequest.of(0, 20));

    ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
    org.mockito.Mockito.verify(elasticsearchOperations)
        .search(queryCaptor.capture(), any(Class.class));

    assertThat(queryCaptor.getValue().getSortOptions()).isNotNull();
    assertThat(queryCaptor.getValue().getSortOptions()).hasSize(1);
    assertThat(queryCaptor.getValue().getSortOptions().get(0).field().field()).isEqualTo("consultedAt");
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("search - 키워드가 있어도 pageable 정렬 조건을 적용한다")
  void search_withKeyword_appliesPageableSort() {
    SearchHits<ConsultSearchDocument> emptyHits = (SearchHits<ConsultSearchDocument>) SearchHits.empty();
    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class))).willReturn(emptyHits);

    ConsultationSearchRequest request = new ConsultationSearchRequest();
    request.setKeyword("해지");

    consultationSearchService.search(request, PageRequest.of(0, 20,
        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC,
            "durationSec")));

    ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
    org.mockito.Mockito.verify(elasticsearchOperations)
        .search(queryCaptor.capture(), any(Class.class));

    assertThat(queryCaptor.getValue().getSortOptions()).isNotNull();
    assertThat(queryCaptor.getValue().getSortOptions()).hasSize(1);
    assertThat(queryCaptor.getValue().getSortOptions().get(0).field().field()).isEqualTo("durationSec");
    assertThat(queryCaptor.getValue().getSortOptions().get(0).field().order().jsonValue())
        .isEqualTo("asc");
  }

}
