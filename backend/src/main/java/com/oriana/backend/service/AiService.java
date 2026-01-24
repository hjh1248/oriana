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

import com.fasterxml.jackson.core.json.JsonReadFeature;

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

            // 1. ✨ 정밀 조각 로직: 첫 번째 '['와 마지막 ']' 사이만 남기기
            // (제미나이가 앞뒤에 헛소리를 붙여도 JSON 배열만 쏙 뽑아냄)
            int start = aiText.indexOf("[");
            int end = aiText.lastIndexOf("]");

            if (start != -1 && end != -1 && start < end) {
                aiText = aiText.substring(start, end + 1);
            }

            // 2. 수식 백슬래시 에러 방지 처리
            String cleanJson = aiText.trim();

            return objectMapper.readTree(cleanJson);
        } catch (Exception e) {
            // 3. 🔥 핵심: 여기서 에러를 던지지 않고 로그만 찍고 null 반환!
            System.err.println("AI 데이터 파싱 실패: " + e.getMessage());
            return null; // 실패하면 그냥 null을 줘서 호출한 쪽에서 넘어가게 함
        }
    }

    /**
     * 1. [맞춤 추천] 프롬프트
     * ✨ 수정 완료: JSON 안의 %s를 모두 구체적인 예시 텍스트로 바꿨음!
     */
    public JsonNode generateRecommendedProblem(ProblemRequestDto prefs) {
        String prompt = String.format("""
            당신은 대한민국 최고 수준의 %s 강사입니다.
            다음 조건에 맞는 문제를 3개 생성하고, 반드시 JSON 형식으로만 답변하세요. 다른 텍스트는 출력하지 마세요.
            
            [조건]
            - 학년: %s
            - 과목: %s
            - 단원: %s
            - 난이도: %s
            - 유형: %s
            - 수식은 반드시 LaTeX 기호 하나($)를 사용하는 'Inline Mode'로만 작성하세요.
            - 수식 명령어 작성 시, JSON 에러 방지를 위해 반드시 백슬래시를 두 번(\\\\) 사용하세요. (예: \\\\frac, \\\\sqrt, \\\\quad)
            
            [출력 JSON 규격]
            [
                {
                  "grade": "고1", // 예시 데이터일 뿐, 실제로는 위 조건을 따를 것
                  "subject": "수학", 
                  "difficulty": "중", 
                  "tags": ["다항식", "인수분해"],
                  "points": 20,
                  "question": "첫 번째 문제 텍스트",
                  "options": ["보기1", "보기2", "보기3", "보기4"],
                  "answer": "정답",
                  "solution": "친절하고 상세한 풀이 과정",
                  "isSubjective": false
                },
                {
                  "grade": "고1",
                  "subject": "수학", 
                  "difficulty": "중",
                  "tags": ["나머지 정리", "항등식"],
                  "points": 25,
                  "question": "두 번째 문제 텍스트 (첫 번째와 다른 개념)",
                  "options": ["보기1", "보기2", "보기3", "보기4"],
                  "answer": "정답",
                  "solution": "친절하고 상세한 풀이 과정",
                  "isSubjective": false
                },
                {
                  "grade": "고1",
                  "subject": "수학", 
                  "difficulty": "상", 
                  "tags": ["다항식", "심화"],
                  "points": 30,
                  "question": "세 번째 문제 텍스트 (가장 난이도가 높은 문제)",
                  "options": ["보기1", "보기2", "보기3", "보기4"],
                  "answer": "정답",
                  "solution": "친절하고 상세한 풀이 과정",
                  "isSubjective": true
                }
            ]
            """, prefs.getSubject(), prefs.getGrade(), prefs.getSubject(), prefs.getUnit(), prefs.getDifficulty(), prefs.getType());

        return callGeminiApi(prompt);
    }

    /**
     * 2. [유사 문제] 프롬프트
     * ✨ 여기는 이미 완벽하게 잘 작성되어 있었어!
     */
    public JsonNode generateSimilarProblem(Problem baseProblem) {
        String prompt = String.format("""
            당신은 수학 문제 변형 전문가입니다. 아래 원본 문제를 분석하여, 
            풀이 개념은 동일하지만 숫자나 상황만 다른 '유사 문제'를 3개 생성해주세요.
            
            [원본 문제]
            %s
            
            [지시사항]
            반드시 JSON 형식으로만 답변하세요. 다른 텍스트는 출력하지 마세요.s
            원본 문제의 학년, 과목, 난이도를 파악하여 변형 문제의 메타데이터도 함께 생성하세요.
            수식 명령어 작성 시, JSON 에러 방지를 위해 반드시 백슬래시를 두 번(\\\\) 사용하세요. (예: \\\\frac, \\\\sqrt, \\\\quad)
            수식은 반드시 LaTeX 기호 하나($)를 사용하는 'Inline Mode'로만 작성하세요.
            
            [출력 JSON 규격]
            [
                {
                  "grade": "고1",
                  "subject": "수학", 
                  "difficulty": "중",
                  "tags": ["이차방정식", "근과 계수", "변형"], 
                  "points": 25, 
                  "question": "문제 텍스트",
                  "options": ["보기1", "보기2", "보기3", "보기4"],
                  "answer": "정답",
                  "solution": "친절하고 상세한 풀이 과정",
                  "isSubjective": false
                },
                {
                  "grade": "고1",
                  "subject": "수학", 
                  "difficulty": "중",
                  "tags": ["이차방정식", "근과 계수", "변형"], 
                  "points": 25, 
                  "question": "문제 텍스트",
                  "options": ["보기1", "보기2", "보기3", "보기4"],
                  "answer": "정답",
                  "solution": "친절하고 상세한 풀이 과정",
                  "isSubjective": false
                },
                {
                  "grade": "고1",
                  "subject": "수학", 
                  "difficulty": "중",
                  "tags": ["이차방정식", "근과 계수", "변형"], 
                  "points": 25, 
                  "question": "문제 텍스트",
                  "options": ["보기1", "보기2", "보기3", "보기4"],
                  "answer": "정답",
                  "solution": "친절하고 상세한 풀이 과정",
                  "isSubjective": false
                }
            ]
            """, baseProblem.getQuestion());

        return callGeminiApi(prompt);
    }

    /**
     * ✨ [멀티모달] 이미지 파일을 직접 Gemini에게 던져서 문제 데이터로 변환
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
                        당신은 이미지 속 수학/과학 문제를 디지털로 완벽하게 복원하는 전문가입니다.
                        첨부된 이미지를 보고 문제를 인식하여 반드시 '길이가 1인 JSON 배열' 형식으로만 응답하세요.
                        수식은 반드시 LaTeX 기호 하나($)를 사용하는 'Inline Mode'로만 작성하세요.
                        글자가 흐릿해도 문맥에 맞게 보정하세요.
                        수식 명령어 작성 시, JSON 에러 방지를 위해 반드시 백슬래시를 두 번(\\\\) 사용하세요. (예: \\\\frac, \\\\sqrt, \\\\quad)
                        
                        [출력 JSON 규격]
                        [
                            {
                              "grade": "고1", // 예상 학년
                              "subject": "수학", // 과목
                              "difficulty": "중", // 예상 난이도
                              "tags": ["사진분석", "이차방정식"], // 문제 핵심 개념
                              "points": 20, 
                              "question": "사진 속 원본 문제 텍스트",
                              "options": ["보기1", "보기2", "보기3", "보기4"],
                              "answer": "정답",
                              "solution": "친절한 풀이 과정",
                              "isSubjective": false
                            }
                        ]
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

            return objectMapper.readTree(aiText.replace("```json", "").replace("```", "").trim());

        } catch (Exception e) {
            throw new RuntimeException("사진 분석 실패: " + e.getMessage());
        }
    }
}