package com.uplus.crm;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {

    private static final String INDEX_NAME = "consult-index";

    private final RestClient restClient;

    @PostConstruct
    public void createIndex() {
        try {
            // 1. 인덱스 이미 존재하면 스킵
            Request headRequest = new Request("HEAD", "/" + INDEX_NAME);
            try {
                restClient.performRequest(headRequest);
                System.out.println("[ES] Index already exists, skipping: " + INDEX_NAME);
                return;
            } catch (Exception ignored) {
                // 404 응답 = 인덱스 없음 → 생성 진행
            }

            // 2. settings + mappings 파일 로드
            InputStream settingsIs = getClass().getClassLoader()
                    .getResourceAsStream("elasticsearch/consult-settings.json");
            InputStream mappingsIs = getClass().getClassLoader()
                    .getResourceAsStream("elasticsearch/consult-mapping.json");

            if (settingsIs == null || mappingsIs == null) {
                System.err.println("[ES] Index config file not found, skipping index creation.");
                return;
            }

            String settings = new String(settingsIs.readAllBytes());
            String mappings = new String(mappingsIs.readAllBytes());

            // 3. Elasticsearch PUT index API 형식에 맞게 settings 래퍼 추가
            String body = """
                    {
                      "settings": %s,
                      "mappings": %s
                    }
                    """.formatted(settings, mappings);

            // 4. 인덱스 생성
            Request request = new Request("PUT", "/" + INDEX_NAME);
            request.setJsonEntity(body);
            restClient.performRequest(request);
            System.out.println("[ES] Index created successfully: " + INDEX_NAME);

        } catch (Exception e) {
            System.err.println("[ES] Index creation failed (app startup continues): " + e.getMessage());
        }
    }
}
