package com.uplus.crm.domain.summary.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.elasticsearch.service.ConsultSearchService;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import com.uplus.crm.domain.summary.dto.request.SummarySearchRequest;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDetailResponse;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryListResponse;
import com.uplus.crm.domain.summary.repository.SummaryConsultationResultRepository;
import com.uplus.crm.domain.summary.repository.SummaryRepository;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SummaryService {

  private final SummaryRepository summaryRepository;
  private final SummaryConsultationResultRepository consultationResultRepository;
  private final MongoTemplate mongoTemplate;
  private final ConsultSearchService consultSearchService;

  /**
   * Hybrid 검색 — ES(keyword) + MongoDB(조건절) 결합
   *
   * <ul>
   *   <li>keyword: ES → consultId 목록 → MongoDB IN 필터 (ES 결과 없으면 MongoDB regex fallback)</li>
   *   <li>consultantName, customerName, productName: MongoDB regex</li>
   *   <li>날짜·카테고리·채널·고객정보·위험유형·만족도: MongoDB Criteria</li>
   * </ul>
   */
  public Page<ConsultationSummaryListResponse> search(
      SummarySearchRequest req, Pageable pageable) {

    Criteria criteria = buildCriteria(req);
    Query query = new Query(criteria).with(pageable);
    long total = mongoTemplate.count(new Query(criteria), ConsultationSummary.class);
    List<ConsultationSummary> content =
        mongoTemplate.find(query, ConsultationSummary.class);

    return new PageImpl<>(
        content.stream().map(ConsultationSummaryListResponse::from).toList(),
        pageable,
        total);
  }

  public ConsultationSummaryDetailResponse getDetail(Long id) {
    if (!consultationResultRepository.existsById(id)) {
      throw new BusinessException(ErrorCode.CONSULTATION_RESULT_NOT_FOUND);
    }

    ConsultationSummary entity =
        summaryRepository.findByConsultId(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.SUMMARY_NOT_FOUND));

    return ConsultationSummaryDetailResponse.from(entity);
  }

  // ── 추천 검색어 ────────────────────────────────────────────────────────────

  /**
   * IAM 기반 검색 추천 키워드 반환.
   *
   * <ul>
   *   <li>AI가 추출한 {@code summary.keywords} 배열을 주 소스로 집계</li>
   *   <li>부족하면 {@code iam.matchKeyword} 배열에서 보충</li>
   *   <li>prefix 미입력 시 전체 빈도 Top N 반환</li>
   * </ul>
   */
  public List<String> suggestKeywords(String prefix, int limit) {
    String safePrefix = (prefix != null && !prefix.isBlank())
        ? Pattern.quote(prefix.trim()) : null;

    Set<String> result = new LinkedHashSet<>();
    result.addAll(aggregateKeywordsFromField("summary.keywords", safePrefix, limit));

    if (result.size() < limit) {
      aggregateKeywordsFromField("iam.matchKeyword", safePrefix, limit - result.size())
          .stream().filter(k -> !result.contains(k)).forEach(result::add);
    }
    return List.copyOf(result);
  }

  private List<String> aggregateKeywordsFromField(String field, String safePrefix, int n) {
    List<AggregationOperation> ops = new ArrayList<>();

    if (safePrefix != null) {
      ops.add(Aggregation.match(Criteria.where(field).regex("^" + safePrefix, "i")));
    } else {
      ops.add(Aggregation.match(Criteria.where(field).exists(true)));
    }
    ops.add(Aggregation.unwind("$" + field));
    if (safePrefix != null) {
      ops.add(Aggregation.match(Criteria.where(field).regex("^" + safePrefix, "i")));
    }
    ops.add(Aggregation.group(field).count().as("cnt"));
    ops.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "cnt")));
    ops.add(Aggregation.limit(n));

    return mongoTemplate
        .aggregate(Aggregation.newAggregation(ops), ConsultationSummary.class, Document.class)
        .getMappedResults().stream()
        .map(d -> d.getString("_id"))
        .filter(Objects::nonNull)
        .toList();
  }

  // ── private ────────────────────────────────────────────────────────────────

  private Criteria buildCriteria(SummarySearchRequest req) {
    List<Criteria> conditions = new ArrayList<>();

    // ── keyword: ES Hybrid ────────────────────────────────────────────────
    // ES로 consultId 목록을 조회 → MongoDB IN 필터.
    // ES에 consultId 가 없는 경우(테스트 데이터 등) MongoDB regex fallback.
    if (StringUtils.hasText(req.getKeyword())) {
      List<Long> esIds = consultSearchService.searchConsultIdsByKeyword(req.getKeyword());
      if (!esIds.isEmpty()) {
        conditions.add(Criteria.where("consultId").in(esIds));
      } else {
        // fallback: MongoDB 전문 regex
        Pattern kw = iPattern(req.getKeyword());
        conditions.add(new Criteria().orOperator(
            Criteria.where("iam.issue").regex(kw),
            Criteria.where("iam.action").regex(kw),
            Criteria.where("iam.memo").regex(kw),
            Criteria.where("summary.content").regex(kw),
            Criteria.where("summary.keywords").regex(kw)
        ));
      }
    }

    // ── 상담 기간 ─────────────────────────────────────────────────────────
    if (req.getFrom() != null || req.getTo() != null) {
      Criteria date = Criteria.where("consultedAt");
      if (req.getFrom() != null) date = date.gte(req.getFrom().atStartOfDay());
      if (req.getTo()   != null) date = date.lte(req.getTo().atTime(LocalTime.MAX));
      conditions.add(date);
    }

    // ── 담당 상담사 이름 (부분 일치) ──────────────────────────────────────
    if (StringUtils.hasText(req.getConsultantName())) {
      conditions.add(Criteria.where("agent.name").regex(iPattern(req.getConsultantName())));
    }

    // ── 상담 카테고리명 (large / medium / small OR) ───────────────────────
    if (StringUtils.hasText(req.getCategoryName())) {
      Pattern cat = iPattern(req.getCategoryName());
      conditions.add(new Criteria().orOperator(
          Criteria.where("category.large").regex(cat),
          Criteria.where("category.medium").regex(cat),
          Criteria.where("category.small").regex(cat)
      ));
    }

    // ── 상담 채널 (CALL / CHATTING) ───────────────────────────────────────
    if (StringUtils.hasText(req.getChannel())) {
      conditions.add(Criteria.where("channel").is(req.getChannel()));
    }

    // ── 고객 이름 (부분 일치) ──────────────────────────────────────────────
    if (StringUtils.hasText(req.getCustomerName())) {
      conditions.add(Criteria.where("customer.name").regex(iPattern(req.getCustomerName())));
    }

    // ── 고객 연락처 (부분 일치) ────────────────────────────────────────────
    if (StringUtils.hasText(req.getCustomerPhone())) {
      conditions.add(Criteria.where("customer.phone").regex(iPattern(req.getCustomerPhone())));
    }

    // ── 고객 유형 (정확 일치) ──────────────────────────────────────────────
    if (StringUtils.hasText(req.getCustomerType())) {
      conditions.add(Criteria.where("customer.type").is(req.getCustomerType()));
    }

    // ── 고객 등급 (복수 선택 — IN) ────────────────────────────────────────
    if (req.getCustomerGrades() != null && !req.getCustomerGrades().isEmpty()) {
      conditions.add(Criteria.where("customer.grade").in(req.getCustomerGrades()));
    }

    // ── 위험 유형 (복수 선택 — riskFlags 배열 OR) ─────────────────────────
    if (req.getRiskTypes() != null && !req.getRiskTypes().isEmpty()) {
      conditions.add(Criteria.where("riskFlags").in(req.getRiskTypes()));
    }

    // ── 상품명 (resultProducts subscribed / canceled 배열 OR 부분 일치) ───
    if (StringUtils.hasText(req.getProductName())) {
      Pattern prod = iPattern(req.getProductName());
      conditions.add(new Criteria().orOperator(
          Criteria.where("resultProducts.subscribed").regex(prod),
          Criteria.where("resultProducts.canceled").regex(prod)
      ));
    }

    // ── 고객만족도 최소값 (customer.satisfiedScore >= satisfactionScore) ───
    if (req.getSatisfactionScore() != null) {
      conditions.add(Criteria.where("customer.satisfiedScore")
          .gte(req.getSatisfactionScore().doubleValue()));
    }

    if (conditions.isEmpty()) {
      return new Criteria();
    }
    return new Criteria().andOperator(conditions.toArray(new Criteria[0]));
  }

  /** 대소문자 무시 Pattern */
  private static Pattern iPattern(String text) {
    return Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE);
  }
}
