package com.uplus.crm.domain.elasticsearch.service;

import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import com.uplus.crm.domain.elasticsearch.repository.ConsultElasticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultSearchService {

    private final ConsultElasticRepository consultElasticRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 상담 데이터 저장 (Indexing)
     */
    public void saveConsultation(ConsultDoc consultDoc) {
        consultElasticRepository.save(consultDoc);
    }

    /**
     * 통합 키워드 검색 (동의어 및 가중치 적용)
     * "갤폰"으로 검색해도 "갤럭시"가 포함된 iam_issue(가중치 2배)와 content에서 찾아옵니다.
     */
    public List<ConsultDoc> searchByKeyword(String keyword) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .fields("iam_issue^2.0", "content", "all_text")
                                .query(keyword)
                        )
                )
                .build();

        SearchHits<ConsultDoc> searchHits = elasticsearchOperations.search(query, ConsultDoc.class);

        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}