package com.oriana.backend.service;

import com.oriana.backend.dto.AiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    // Gemini 1.5 Flash 모델 사용 (빠르고 무료 티어 있음)
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    public AiResponseDto analyzeImage(MultipartFile imageFile) {
        try {
            // 1. 이미지를 Base64로 변환
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());

            // 2. 프롬프트 준비 (JSON으로 달라고 강력 요청)
            String promptText = """
                너는 '수학/과학 선생님'이야. 이 오답 이미지를 분석해줘.
                
                1. 문제 원문 텍스트 (수식은 LaTeX)
                2. 핵심 개념 (한 문단)
                3. 상세 풀이
                4. 유사 문제 3개 (5지선다 객관식)
                
                **반드시 아래 JSON 스키마를 준수해서 답변해:**
                {
                  "original_problem": "string",
                  "concept": "string",
                  "solution": "string",
                  "similar_questions": [
                    { "question": "string", "options": ["1. 보기", ...], "answer": "string" }
                  ]
                }
                """;

            // 3. 요청 바디 구성 (Gemini 스타일)
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", promptText),
                                    Map.of("inline_data", Map.of(
                                            "mime_type", "image/jpeg", // png면 image/png
                                            "data", base64Image
                                    ))
                            ))
                    ),
                    "generationConfig", Map.of(
                            "response_mime_type", "application/json" // JSON 강제 모드 (중요!)
                    )
            );

            // 4. API 호출
            RestClient restClient = RestClient.create();
            var response = restClient.post()
                    .uri(GEMINI_URL + "?key=" + apiKey) // URL 쿼리 파라미터로 키 전송
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            // 5. 응답 파싱
            // 구조: candidates[0] -> content -> parts[0] -> text
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String jsonString = (String) parts.get(0).get("text");

            // 6. JSON 문자열 -> DTO 변환
            return objectMapper.readValue(jsonString, AiResponseDto.class);

        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생", e);
            throw new RuntimeException("AI 선생님이 잠깐 쉬고 싶대요.. (API 오류)");
        }
    }
}