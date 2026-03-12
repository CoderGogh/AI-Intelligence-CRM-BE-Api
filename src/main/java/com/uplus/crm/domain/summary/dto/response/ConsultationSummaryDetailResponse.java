package com.uplus.crm.domain.summary.dto.response;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultationSummaryDetailResponse {

  private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

  private String id;
  private Long consultId;
  private LocalDateTime consultedAt;
  private LocalDateTime createdAt;
  private Integer durationSec;
  private String channel;

  private CategoryInfo category;
  private AgentInfo agent;
  private CustomerInfo customer;
  private IamInfo iam;
  private SummaryInfo summary;
  private List<RiskFlagInfo> riskFlags;
  private CancellationInfo cancellation;
  private List<ProductInfo> resultProducts;
  private List<ProductInfo> products;

  @Getter @Builder
  public static class CategoryInfo {
    private String code;
    private String large;
    private String medium;
    private String small;
  }

  @Getter @Builder
  public static class AgentInfo {
    private Long id;
    private String name;
  }

  @Getter @Builder
  public static class CustomerInfo {
    private Long id;
    private String grade;
    private String name;
    private String phone;
    private String type;
    private String ageGroup;
    private Double satisfiedScore;
  }

  @Getter @Builder
  public static class IamInfo {
    private String memo;
    private String action;
    private String issue;
    private List<String> matchKeyword;
    private Double matchRate;
  }

  @Getter @Builder
  public static class SummaryInfo {
    private String status;
    private String content;
    private List<String> keywords;
  }

  @Getter @Builder
  public static class RiskFlagInfo {
    private String riskType;
    private String riskLevel;
  }

  @Getter @Builder
  public static class CancellationInfo {
    private Boolean intent;
    private Boolean defenseAttempted;
    private Boolean defenseSuccess;
    private List<String> defenseActions;
    private String complaintReasons;
  }

  @Getter @Builder
  public static class ProductInfo {
    private List<String> subscribed;
    private List<String> canceled;
    private List<ConsultationSummary.ResultProducts.Conversion> conversion;
    private List<String> recommitment;
    private String changeType;
  }

  public static ConsultationSummaryDetailResponse from(
      ConsultationSummary e) {

    return ConsultationSummaryDetailResponse.builder()
        .id(e.getId())
        .consultId(e.getConsultId())
        .consultedAt(convertUtcToKst(e.getConsultedAt()))
        .createdAt(convertUtcToKst(e.getCreatedAt()))
        .durationSec(e.getDurationSec())
        .channel(e.getChannel())

        .category(e.getCategory() == null ? null :
            CategoryInfo.builder()
                .code(e.getCategory().getCode())
                .large(e.getCategory().getLarge())
                .medium(e.getCategory().getMedium())
                .small(e.getCategory().getSmall())
                .build())

        .agent(e.getAgent() == null ? null :
            AgentInfo.builder()
                .id(e.getAgent().get_id())
                .name(e.getAgent().getName())
                .build())

        .customer(e.getCustomer() == null ? null :
            CustomerInfo.builder()
                .id(e.getCustomer().get_id())
                .grade(e.getCustomer().getGrade())
                .name(e.getCustomer().getName())
                .phone(e.getCustomer().getPhone())
                .type(e.getCustomer().getType())
                .ageGroup(e.getCustomer().getAgeGroup())
                .satisfiedScore(e.getCustomer().getSatisfiedScore())
                .build())

        .iam(e.getIam() == null ? null :
            IamInfo.builder()
                .memo(e.getIam().getMemo())
                .action(e.getIam().getAction())
                .issue(e.getIam().getIssue())
                .matchKeyword(e.getIam().getMatchKeyword())
                .matchRate(e.getIam().getMatchRates())
                .build())

        .summary(e.getSummary() == null ? null :
            SummaryInfo.builder()
                .status(e.getSummary().getStatus())
                .content(e.getSummary().getContent())
                .keywords(e.getSummary().getKeywords())
                .build())

        .riskFlags(e.getRiskFlags() == null ? null :
            e.getRiskFlags().stream()
                .map(flag -> RiskFlagInfo.builder()
                    .riskType(flag.getRiskType())
                    .riskLevel(flag.getRiskLevel())
                    .build())
                .toList())

        .cancellation(e.getCancellation() == null ? null :
            CancellationInfo.builder()
                .intent(e.getCancellation().getIntent())
                .defenseAttempted(e.getCancellation().getDefenseAttempted())
                .defenseSuccess(e.getCancellation().getDefenseSuccess())
                .defenseActions(e.getCancellation().getDefenseActions())
                .complaintReasons(e.getCancellation().getComplaintReasons())
                .build())

        .resultProducts(buildProductInfo(e))
        .products(buildProductInfo(e))

        .build();
  }

  private static List<ProductInfo> buildProductInfo(ConsultationSummary summary) {
    if (summary.getResultProducts() == null) {
      return null;
    }

    return summary.getResultProducts().stream()
        .map(p -> ProductInfo.builder()
            .subscribed(p.getSubscribed())
            .canceled(p.getCanceled())
            .conversion(p.getConversion())
            .recommitment(p.getRecommitment())
            .changeType(p.getChangeType())
            .build())
        .toList();
  }

  private static LocalDateTime convertUtcToKst(LocalDateTime utcDateTime) {
    if (utcDateTime == null) {
      return null;
    }

    return utcDateTime.atZone(ZoneOffset.UTC)
        .withZoneSameInstant(KST_ZONE)
        .toLocalDateTime();
  }
}
