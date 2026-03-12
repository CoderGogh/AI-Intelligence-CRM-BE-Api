package com.uplus.crm.domain.summary.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsultationSummaryDetailResponseTest {

  @Test
  @DisplayName("상세 응답 매핑 시 ConsultationSummary 전체 필드와 KST 시간 변환을 반영한다")
  void from_mapsAllFieldsAndConvertsUtcToKst() {
    ConsultationSummary summary = ConsultationSummary.builder()
        .id("mongo-id")
        .consultId(101L)
        .consultedAt(LocalDateTime.of(2026, 3, 5, 1, 20, 30))
        .createdAt(LocalDateTime.of(2026, 3, 5, 3, 0, 0))
        .durationSec(185)
        .channel("INBOUND")
        .agent(ConsultationSummary.Agent.builder()._id(7L).name("상담사A").build())
        .category(ConsultationSummary.Category.builder()
            .code("C001")
            .large("요금")
            .medium("할인")
            .small("재약정")
            .build())
        .iam(ConsultationSummary.Iam.builder()
            .issue("해지문의")
            .action("혜택제시")
            .memo("고객 유지 의사")
            .matchKeyword(List.of("해지", "유지"))
            .matchRates(0.95)
            .build())
        .summary(ConsultationSummary.Summary.builder()
            .status("DONE")
            .content("상담 요약")
            .keywords(List.of("요금", "혜택"))
            .build())
        .riskFlags(List.of(
            ConsultationSummary.RiskFlag.builder().riskType("민원").riskLevel("HIGH").build(),
            ConsultationSummary.RiskFlag.builder().riskType("이탈").riskLevel("MEDIUM").build()))
        .customer(ConsultationSummary.Customer.builder()
            ._id(9L)
            .type("개인")
            .phone("010-1234-5678")
            .name("홍길동")
            .ageGroup("30대")
            .grade("VIP")
            .satisfiedScore(4.5)
            .build())
        .cancellation(ConsultationSummary.Cancellation.builder()
            .intent(true)
            .defenseAttempted(true)
            .defenseSuccess(false)
            .defenseActions(List.of("요금할인", "사은품"))
            .complaintReasons("요금 부담")
            .build())
        .resultProducts(List.of(
            ConsultationSummary.ResultProducts.builder()
                .subscribed(List.of("인터넷"))
                .canceled(List.of("TV"))
                .conversion(List.of(ConsultationSummary.ResultProducts.Conversion.builder()
                    .subscribed("인터넷")
                    .canceled("TV")
                    .build()))
                .recommitment(List.of("12개월"))
                .changeType("UPSELL")
                .build()))
        .build();

    ConsultationSummaryDetailResponse response = ConsultationSummaryDetailResponse.from(summary);

    assertThat(response.getId()).isEqualTo("mongo-id");
    assertThat(response.getConsultId()).isEqualTo(101L);
    assertThat(response.getConsultedAt()).isEqualTo(LocalDateTime.of(2026, 3, 5, 10, 20, 30));
    assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 3, 5, 12, 0, 0));
    assertThat(response.getDurationSec()).isEqualTo(185);
    assertThat(response.getChannel()).isEqualTo("INBOUND");

    assertThat(response.getCategory().getCode()).isEqualTo("C001");
    assertThat(response.getAgent().getId()).isEqualTo(7L);

    assertThat(response.getCustomer().getId()).isEqualTo(9L);
    assertThat(response.getCustomer().getSatisfiedScore()).isEqualTo(4.5);
    assertThat(response.getCustomer().getSatisfaction()).isEqualTo("4.5");

    assertThat(response.getIam().getMatchKeyword()).containsExactly("해지", "유지");
    assertThat(response.getSummary().getStatus()).isEqualTo("DONE");
    assertThat(response.getRiskFlags()).hasSize(2);

    assertThat(response.getResultProducts()).hasSize(1);
    assertThat(response.getProducts()).hasSize(1);
    assertThat(response.getResultProducts().get(0).getChangeType()).isEqualTo("UPSELL");
  }
}
