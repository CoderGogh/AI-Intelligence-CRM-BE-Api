package com.uplus.crm.domain.elasticsearch.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "consult-index")
@Setting(settingPath = "elasticsearch/consult-index.json")
public class ConsultDoc {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "consult_index_analyzer", searchAnalyzer = "consult_search_analyzer")
    private String content;

    // [수정] boost 속성 제거 (쿼리 시점에 적용합니다)
    @Field(type = FieldType.Text, analyzer = "consult_index_analyzer", searchAnalyzer = "consult_search_analyzer")
    private String iamIssue;

    @Field(type = FieldType.Text, analyzer = "consult_index_analyzer", searchAnalyzer = "consult_search_analyzer")
    private String iamAction;

    @Field(type = FieldType.Text, analyzer = "consult_index_analyzer", searchAnalyzer = "consult_search_analyzer")
    private String iamMemo;

    @Field(type = FieldType.Text, analyzer = "consult_index_analyzer", searchAnalyzer = "consult_search_analyzer")
    private String allText;

    @Field(type = FieldType.Keyword)
    private String sentiment;

    @Field(type = FieldType.Integer)
    private Integer riskScore;

    @Field(type = FieldType.Keyword)
    private String priority;

    @Field(type = FieldType.Keyword)
    private String customerId;

    // [수정] @InnerField 에러 해결: @MultiField를 사용해야 합니다.
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "consult_index_analyzer"),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword)
            }
    )
    private String customerName;

    @Field(type = FieldType.Keyword)
    private String phone;

    // [수정] DateFormat.custom 에러 해결: pattern을 바로 쓰거나 다른 enum 사용
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}