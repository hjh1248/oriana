package com.oriana.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriana.backend.domain.Problem;
import com.oriana.backend.dto.ProblemRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent";

    private JsonNode callGeminiApi(String prompt) {
        String url = GEMINI_URL + "?key=" + geminiApiKey;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", new Object[]{
                Map.of("parts", new Object[]{Map.of("text", prompt)})
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            String responseStr = restTemplate.postForObject(url, request, String.class);
            JsonNode root = objectMapper.readTree(responseStr);
            String aiText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            // 개선된 JSON 추출 로직
            String cleanJson = extractJsonArray(aiText);

            // JSON 파싱 전 추가 정제
            cleanJson = sanitizeJsonString(cleanJson);

            return objectMapper.readTree(cleanJson);
        } catch (Exception e) {
            System.err.println("AI 데이터 파싱 실패: " + e.getMessage());
            System.err.println("응답 원본: " + (responseStr != null ? responseStr.substring(0, Math.min(500, responseStr.length())) : "null"));
            return null;
        }
    }

    /**
     * JSON 배열 추출 - 더 견고한 방식
     */
    private String extractJsonArray(String text) {
        // 1. Markdown 코드 블록 제거
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");

        // 2. 앞뒤 공백 제거
        text = text.trim();

        // 3. 첫 번째 '['를 찾고, 매칭되는 ']'를 찾기 (중첩 배열 고려)
        int firstBracket = text.indexOf('[');
        if (firstBracket == -1) {
            return text; // 배열이 없으면 원본 반환
        }

        int depth = 0;
        int endBracket = -1;

        for (int i = firstBracket; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    endBracket = i;
                    break;
                }
            }
        }

        if (endBracket != -1) {
            return text.substring(firstBracket, endBracket + 1);
        }

        // 매칭 실패 시 기존 방식으로 폴백
        int lastBracket = text.lastIndexOf(']');
        if (lastBracket != -1) {
            return text.substring(firstBracket, lastBracket + 1);
        }

        return text;
    }

    /**
     * JSON 문자열 정제 - 파싱 오류 방지
     */
    private String sanitizeJsonString(String json) {
        // 1. 제어 문자 제거 (탭, 개행 등은 유지)
        json = json.replaceAll("[\\x00-\\x09\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");

        // 2. 불필요한 이스케이프 정리 (이미 이스케이프된 경우)
        // LaTeX의 경우 \\frac이나 \\\\frac 둘 다 허용되도록 유연하게 처리

        return json;
    }

    /**
     * 1. [맞춤 추천] 프롬프트 - 개선된 버전
     */
    public JsonNode generateRecommendedProblem(ProblemRequestDto prefs) {
        String prompt = String.format("""
            당신은 대한민국 최고 수준의 %s 강사입니다.
            다음 조건에 맞는 문제를 3개 생성하고, 반드시 JSON 배열 형식으로만 답변하세요.
            
            ⚠️ 중요: 다른 설명이나 텍스트 없이 오직 JSON 배열만 출력하세요. ```json 같은 마크다운 코드블록도 사용하지 마세요.
            
            [조건]
            - 학년: %s
            - 과목: %s
            - 단원: %s
            - 난이도: %s
            - 유형: %s
            
            [수식 작성 규칙]
            - LaTeX 수식은 인라인 모드($...$)로 작성
            - 백슬래시는 한 번만 사용 (\\frac, \\sqrt 등)
            - JSON 문자열 내부이므로 특수문자 이스케이프에 주의
            
            [출력 형식]
            [
                {
                  "grade": "%s",
                  "subject": "%s",
                  "difficulty": "%s",
                  "tags": ["개념1", "개념2"],
                  "points": 20,
                  "question": "문제 텍스트",
                  "options": ["①", "②", "③", "④"],
                  "answer": "정답",
                  "solution": "풀이",
                  "isSubjective": false
                },
                {
                  "grade": "%s",
                  "subject": "%s",
                  "difficulty": "%s",
                  "tags": ["개념3", "개념4"],
                  "points": 25,
                  "question": "문제 텍스트",
                  "options": ["①", "②", "③", "④"],
                  "answer": "정답",
                  "solution": "풀이",
                  "isSubjective": false
                },
                {
                  "grade": "%s",
                  "subject": "%s",
                  "difficulty": "상",
                  "tags": ["심화", "응용"],
                  "points": 30,
                  "question": "문제 텍스트",
                  "options": [],
                  "answer": "정답",
                  "solution": "풀이",
                  "isSubjective": true
                }
            ]
            
            위 형식을 정확히 따라 JSON 배열만 출력하세요.
            """,
                prefs.getSubject(),
                prefs.getGrade(), prefs.getSubject(), prefs.getUnit(), prefs.getDifficulty(), prefs.getType(),
                prefs.getGrade(), prefs.getSubject(), prefs.getDifficulty(),
                prefs.getGrade(), prefs.getSubject(), prefs.getDifficulty(),
                prefs.getGrade(), prefs.getSubject()
        );

        return callGeminiApi(prompt);
    }

    /**
     * 2. [유사 문제] 프롬프트 - 개선된 버전
     */
    public JsonNode generateSimilarProblem(Problem baseProblem) {
        String prompt = String.format("""
            당신은 수학 문제 변형 전문가입니다.
            아래 원본 문제와 동일한 개념이지만 숫자나 상황이 다른 유사 문제 3개를 생성하세요.
            
            [원본 문제]
            %s
            
            ⚠️ 중요: 다른 설명 없이 오직 JSON 배열만 출력하세요. ```json 같은 마크다운도 사용하지 마세요.
            
            [수식 작성 규칙]
            - LaTeX 수식은 인라인 모드($...$)로 작성
            - 백슬래시는 한 번만 사용 (\\frac, \\sqrt 등)
            
            [출력 형식]
            [
                {
                  "grade": "%s",
                  "subject": "%s",
                  "difficulty": "%s",
                  "tags": ["유사", "변형"],
                  "points": %d,
                  "question": "변형된 문제1",
                  "options": ["①", "②", "③", "④"],
                  "answer": "정답",
                  "solution": "풀이",
                  "isSubjective": %s
                },
                {
                  "grade": "%s",
                  "subject": "%s",
                  "difficulty": "%s",
                  "tags": ["유사", "변형"],
                  "points": %d,
                  "question": "변형된 문제2",
                  "options": ["①", "②", "③", "④"],
                  "answer": "정답",
                  "solution": "풀이",
                  "isSubjective": %s
                },
                {
                  "grade": "%s",
                  "subject": "%s",
                  "difficulty": "%s",
                  "tags": ["유사", "변형"],
                  "points": %d,
                  "question": "변형된 문제3",
                  "options": ["①", "②", "③", "④"],
                  "answer": "정답",
                  "solution": "풀이",
                  "isSubjective": %s
                }
            ]
            
            위 형식을 정확히 따라 JSON 배열만 출력하세요.
            """,
                baseProblem.getQuestion(),
                baseProblem.getGrade(), baseProblem.getSubject(), baseProblem.getDifficulty(),
                baseProblem.getPoints(), baseProblem.getIsSubjective(),
                baseProblem.getGrade(), baseProblem.getSubject(), baseProblem.getDifficulty(),
                baseProblem.getPoints(), baseProblem.getIsSubjective(),
                baseProblem.getGrade(), baseProblem.getSubject(), baseProblem.getDifficulty(),
                baseProblem.getPoints(), baseProblem.getIsSubjective()
        );

        return callGeminiApi(prompt);
    }

    /**
     * 3. [멀티모달] 이미지 분석 - 개선된 버전
     */
    public JsonNode analyzePhotoDirectly(MultipartFile imageFile) {
        String url = GEMINI_URL + "?key=" + geminiApiKey;

        try {
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            String mimeType = imageFile.getContentType();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", new Object[]{
                    Map.of("parts", new Object[]{
                            Map.of("text", """
                        당신은 이미지 속 문제를 디지털로 변환하는 전문가입니다.
                        첨부된 이미지를 분석하여 단 1개의 문제 데이터를 JSON 배열로 반환하세요.
                        
                        ⚠️ 중요: 다른 설명 없이 오직 JSON 배열만 출력하세요. ```json 같은 마크다운도 사용하지 마세요.
                        
                        [수식 작성 규칙]
                        - LaTeX 수식은 인라인 모드($...$)로 작성
                        - 백슬래시는 한 번만 사용 (\\frac, \\sqrt 등)
                        
                        [출력 형식]
                        [
                            {
                              "grade": "고1",
                              "subject": "수학",
                              "difficulty": "중",
                              "tags": ["사진분석"],
                              "points": 20,
                              "question": "이미지에서 추출한 문제",
                              "options": ["①", "②", "③", "④"],
                              "answer": "정답",
                              "solution": "풀이",
                              "isSubjective": false
                            }
                        ]
                        
                        위 형식을 정확히 따라 JSON 배열만 출력하세요.
                        """),
                            Map.of("inline_data", Map.of(
                                    "mime_type", mimeType,
                                    "data", base64Image
                            ))
                    })
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String responseStr = restTemplate.postForObject(url, request, String.class);
            JsonNode root = objectMapper.readTree(responseStr);
            String aiText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            String cleanJson = extractJsonArray(aiText);
            cleanJson = sanitizeJsonString(cleanJson);

            return objectMapper.readTree(cleanJson);

        } catch (Exception e) {
            System.err.println("사진 분석 실패: " + e.getMessage());
            return null;
        }
    }
}