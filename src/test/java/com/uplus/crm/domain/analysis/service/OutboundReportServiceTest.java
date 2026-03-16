package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.domain.analysis.dto.outbound.*;
import com.uplus.crm.domain.common.entity.AnalysisCode;
import com.uplus.crm.domain.common.repository.AnalysisCodeRepository;
import com.uplus.crm.domain.common.repository.ConsultationCategoryPolicyRepository;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OutboundReportServiceTest {

    @Mock MongoTemplate mongoTemplate;
    @Mock ConsultationCategoryPolicyRepository categoryPolicyRepository;
    @Mock AnalysisCodeRepository analysisCodeRepository;
    @InjectMocks OutboundReportService service;

    private Document createSnapshot(Document outboundAnalysis) {
        return new Document()
                .append("startAt", LocalDateTime.of(2026, 3, 15, 0, 0))
                .append("endAt", LocalDateTime.of(2026, 3, 16, 0, 0))
                .append("outboundAnalysis", outboundAnalysis);
    }

    private AnalysisCode createAnalysisCode(String codeName, String displayName, String description) {
        AnalysisCode ac = new AnalysisCode();
        ReflectionTestUtils.setField(ac, "codeName", codeName);
        ReflectionTestUtils.setField(ac, "displayName", displayName);
        ReflectionTestUtils.setField(ac, "description", description);
        ReflectionTestUtils.setField(ac, "classification", "outbound_category");
        return ac;
    }

    @Nested
    @DisplayName("KPI 조회")
    class GetKpi {

        @Test
        @DisplayName("정상 조회 - KPI 필드 매핑")
        void success() {
            Document kpi = new Document()
                    .append("totalCount", 100)
                    .append("convertedCount", 30)
                    .append("rejectedCount", 70)
                    .append("conversionRate", 30.0)
                    .append("avgDurationSec", 245.5)
                    .append("estimatedRevenue", 1500000L);
            Document outbound = new Document("kpi", kpi);
            Document snapshot = createSnapshot(outbound);

            given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                    .willReturn(snapshot);

            Optional<OutboundKpiResponse> result = service.getKpi("daily", LocalDate.of(2026, 3, 15));

            assertThat(result).isPresent();
            OutboundKpiResponse r = result.get();
            assertThat(r.getTotalCount()).isEqualTo(100);
            assertThat(r.getConvertedCount()).isEqualTo(30);
            assertThat(r.getRejectedCount()).isEqualTo(70);
            assertThat(r.getConversionRate()).isEqualTo(30.0);
            assertThat(r.getEstimatedRevenue()).isEqualTo(1500000L);
            assertThat(r.getStartAt()).isNotNull();
            assertThat(r.getEndAt()).isNotNull();
        }

        @Test
        @DisplayName("스냅샷 없으면 empty 반환")
        void noSnapshot() {
            given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                    .willReturn(null);

            Optional<OutboundKpiResponse> result = service.getKpi("daily", LocalDate.of(2026, 3, 15));
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("거절 사유 조회")
    class GetCallResults {

        @Test
        @DisplayName("정상 조회 - 거절 사유에 displayName과 description 매핑")
        void rejectReasonMapping() {
            Document callResults = new Document()
                    .append("distribution", List.of(
                            new Document("result", "CONVERTED").append("count", 30),
                            new Document("result", "REJECTED").append("count", 70)
                    ))
                    .append("rejectReasons", List.of(
                            new Document("code", "COST").append("count", 25),
                            new Document("code", "SWITCH").append("count", 20)
                    ));
            Document outbound = new Document("callResults", callResults);
            Document snapshot = createSnapshot(outbound);

            given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                    .willReturn(snapshot);
            given(analysisCodeRepository.findByClassification("outbound_category"))
                    .willReturn(List.of(
                            createAnalysisCode("COST", "비용 부담", "요금·비용 부담을 이유로 거절한 경우"),
                            createAnalysisCode("SWITCH", "타사 전환", "타사 전환 의사가 확고하여 거절한 경우")
                    ));

            Optional<OutboundCallResultResponse> result = service.getCallResults("daily", LocalDate.of(2026, 3, 15));

            assertThat(result).isPresent();
            OutboundCallResultResponse r = result.get();

            // distribution
            assertThat(r.getDistribution().getConverted().getCount()).isEqualTo(30);
            assertThat(r.getDistribution().getRejected().getCount()).isEqualTo(70);

            // reject reasons - DB에서 displayName, description 가져옴
            List<OutboundCallResultResponse.RejectReason> reasons = r.getRejectReasons();
            assertThat(reasons).hasSize(2);
            assertThat(reasons.get(0).getCode()).isEqualTo("COST");
            assertThat(reasons.get(0).getName()).isEqualTo("비용 부담");
            assertThat(reasons.get(0).getDescription()).isEqualTo("요금·비용 부담을 이유로 거절한 경우");
            assertThat(reasons.get(0).getRank()).isEqualTo(1);
            assertThat(reasons.get(1).getCode()).isEqualTo("SWITCH");
            assertThat(reasons.get(1).getName()).isEqualTo("타사 전환");
        }

        @Test
        @DisplayName("DB에 없는 코드는 code를 name으로 사용")
        void unknownCodeFallback() {
            Document callResults = new Document()
                    .append("distribution", List.of(
                            new Document("result", "REJECTED").append("count", 10)
                    ))
                    .append("rejectReasons", List.of(
                            new Document("code", "UNKNOWN_CODE").append("count", 10)
                    ));
            Document outbound = new Document("callResults", callResults);
            Document snapshot = createSnapshot(outbound);

            given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                    .willReturn(snapshot);
            given(analysisCodeRepository.findByClassification("outbound_category"))
                    .willReturn(List.of());

            Optional<OutboundCallResultResponse> result = service.getCallResults("daily", LocalDate.of(2026, 3, 15));

            assertThat(result).isPresent();
            assertThat(result.get().getRejectReasons().get(0).getName()).isEqualTo("UNKNOWN_CODE");
            assertThat(result.get().getRejectReasons().get(0).getDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("히트맵 조회")
    class GetHeatmap {

        @Test
        @DisplayName("정상 조회 - 히트맵 행 매핑")
        void success() {
            Document heatmap = new Document("conversionRate", List.of(
                    new Document("hour", 9).append("days", List.of(10.0, 20.0, 30.0, 40.0, 50.0)),
                    new Document("hour", 10).append("days", List.of(15.0, 25.0, 35.0, 45.0, 55.0))
            ));
            Document outbound = new Document("heatmap", heatmap);
            Document snapshot = createSnapshot(outbound);

            given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                    .willReturn(snapshot);

            Optional<OutboundHeatmapResponse> result = service.getHeatmap("daily", LocalDate.of(2026, 3, 15));

            assertThat(result).isPresent();
            assertThat(result.get().getRows()).hasSize(2);
            assertThat(result.get().getRows().get(0).getHour()).isEqualTo(9);
            assertThat(result.get().getRows().get(0).getDays()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("period별 컬렉션 매핑")
    class PeriodMapping {

        @Test
        @DisplayName("weekly period는 weekly_report_snapshot 조회")
        void weeklyCollection() {
            Document kpi = new Document("totalCount", 50);
            Document outbound = new Document("kpi", kpi);
            Document snapshot = createSnapshot(outbound);

            given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("weekly_report_snapshot")))
                    .willReturn(snapshot);

            Optional<OutboundKpiResponse> result = service.getKpi("weekly", LocalDate.of(2026, 3, 15));
            assertThat(result).isPresent();
            assertThat(result.get().getTotalCount()).isEqualTo(50);
        }

        @Test
        @DisplayName("monthly period는 monthly_report_snapshot 조회")
        void monthlyCollection() {
            Document kpi = new Document("totalCount", 200);
            Document outbound = new Document("kpi", kpi);
            Document snapshot = createSnapshot(outbound);

            given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("monthly_report_snapshot")))
                    .willReturn(snapshot);

            Optional<OutboundKpiResponse> result = service.getKpi("monthly", LocalDate.of(2026, 3, 15));
            assertThat(result).isPresent();
            assertThat(result.get().getTotalCount()).isEqualTo(200);
        }
    }
}
