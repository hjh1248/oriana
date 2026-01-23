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

    // application.yml에 저장된 API 키를 가져옴
    @Value("${gemini.api-key}")
    private String geminiApiKey;

    // ✨ 네가 챙겨준 바로 그 URL! (Gemini 2.5 Flash)
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    /**
     * 공통 AI 호출 메서드 (프롬프트를 보내고 JSON 응답을 받아옴)
     */
    private JsonNode callGeminiApi(String prompt) {
        String url = GEMINI_URL + "?key=" + geminiApiKey;

        // Gemini API 요청 규격 맞추기
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
            
            // AI 응답에서 불필요한 마크다운(```json ... ```) 제거
            return objectMapper.readTree(aiText.replace("```json", "").replace("```", "").trim());
        } catch (Exception e) {
            throw new RuntimeException("AI 문제 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 1. [맞춤 추천] 프롬프트
     */
    public JsonNode generateRecommendedProblem(ProblemRequestDto prefs) {
        String prompt = String.format("""
            당신은 대한민국 최고 수준의 %s 강사입니다.
            다음 조건에 맞는 문제를 하나만 생성하고, 반드시 JSON 형식으로만 답변하세요. 다른 텍스트는 출력하지 마세요.
            
            [조건]
            - 학년: %s
            - 과목: %s
            - 단원: %s
            - 난이도: %s
            - 유형: %s
            - 수식은 반드시 LaTeX($$ 또는 $)를 사용하세요.
            
            [출력 JSON 규격]
            {
              "question": "문제 텍스트",
              "options": ["보기1", "보기2", "보기3", "보기4"], // 주관식이면 빈 배열 []
              "answer": "정답",
              "solution": "친절하고 상세한 풀이 과정",
              "isSubjective": true/false
            }
            """, prefs.getSubject(), prefs.getGrade(), prefs.getSubject(), prefs.getUnit(), prefs.getDifficulty(), prefs.getType());

        return callGeminiApi(prompt);
    }

    /**
     * 2. [유사 문제] 프롬프트
     */
    public JsonNode generateSimilarProblem(Problem baseProblem) {
        String prompt = String.format("""
            당신은 수학 문제 변형 전문가입니다. 아래 원본 문제를 분석하여, 
            풀이 개념은 동일하지만 숫자나 상황만 다른 '쌍둥이 유사 문제'를 생성해주세요.
            
            [원본 문제]
            %s
            
            반드시 JSON 형식으로만 답변하세요. 출력 규격은 [question, options, answer, solution, isSubjective] 입니다.
            """, baseProblem.getQuestion());

        return callGeminiApi(prompt);
    }

    /**
     * ✨ [멀티모달] 이미지 파일을 직접 Gemini에게 던져서 문제 데이터로 변환
     */
    public JsonNode analyzePhotoDirectly(MultipartFile imageFile) {
        String url = GEMINI_URL + "?key=" + geminiApiKey;

        try {
            // 1. 이미지를 Base64 문자열로 변환 (Gemini가 읽을 수 있게)
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            String mimeType = imageFile.getContentType(); // 예: "image/jpeg"

            // 2. 멀티모달 프롬프트 작성 (지시사항 + 이미지 데이터)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", new Object[]{
                    Map.of("parts", new Object[]{
                            // 첫 번째 파트: 텍스트 프롬프트
                            Map.of("text", """
                        당신은 이미지 속 수학/과학 문제를 디지털로 완벽하게 복원하는 전문가입니다.
                        첨부된 이미지를 보고 문제를 인식하여 JSON 형식으로만 응답하세요.
                        수식은 반드시 LaTeX 기호로 변환해야 하며, 글자가 흐릿해도 문맥에 맞게 보정하세요.
                        
                        [출력 JSON 규격]
                        {
                          "grade": "고1", // 예상 학년
                          "subject": "수학", // 과목
                          "difficulty": "중", // 예상 난이도
                          "question": "문제 텍스트",
                          "options": ["보기1", "보기2", "보기3", "보기4"],
                          "answer": "정답",
                          "solution": "친절한 풀이 과정",
                          "isSubjective": false
                        }
                        """),
                            // 두 번째 파트: 이미지 데이터
                            Map.of("inline_data", Map.of(
                                    "mime_type", mimeType,
                                    "data", base64Image
                            ))
                    })
            });

            // 3. API 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            String responseStr = restTemplate.postForObject(url, request, String.class);
            JsonNode root = objectMapper.readTree(responseStr);
            String aiText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            return objectMapper.readTree(aiText.replace("```json", "").replace("```", "").trim());

        } catch (Exception e) {
            throw new RuntimeException("사진 분석 실패: " + e.getMessage());
        }
    }
}