package com.oriana.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oriana.backend.domain.Problem;
import com.oriana.backend.dto.ProblemRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
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
            if (responseStr == null) {
                log.error("Gemini API 응답이 null입니다.");
                return null;
            }

            JsonNode root = objectMapper.readTree(responseStr);
            String aiText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            log.info("=== AI 원본 응답 (처음 500자) ===\n{}", aiText.substring(0, Math.min(500, aiText.length())));

            // 1단계: JSON 배열 추출
            String jsonArray = extractJsonArray(aiText);
            log.info("=== JSON 추출 완료 (길이: {}) ===", jsonArray.length());

            // 2단계: JSON 정제 (가장 중요!)
            String cleanJson = sanitizeJson(jsonArray);
            log.info("=== JSON 정제 완료 ===");

            // 3단계: 파싱 시도
            try {
                return objectMapper.readTree(cleanJson);
            } catch (JsonProcessingException e) {
                log.error("=== JSON 파싱 1차 실패, 추가 복구 시도 ===");
                log.error("파싱 실패한 JSON (처음 1000자):\n{}", cleanJson.substring(0, Math.min(1000, cleanJson.length())));

                // 최후의 수단: 더 공격적인 정제
                String recoveredJson = aggressiveJsonRecovery(cleanJson);
                return objectMapper.readTree(recoveredJson);
            }

        } catch (Exception e) {
            log.error("AI 데이터 파싱 최종 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * JSON 배열 추출 - 중첩 배열 지원
     */
    private String extractJsonArray(String text) {
        // Markdown 코드 블록 제거
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        text = text.trim();

        int firstBracket = text.indexOf('[');
        if (firstBracket == -1) {
            return text;
        }

        // 매칭되는 닫는 괄호 찾기 (중첩 고려)
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

        // 매칭 실패 시 마지막 ] 사용
        int lastBracket = text.lastIndexOf(']');
        if (lastBracket != -1) {
            return text.substring(firstBracket, lastBracket + 1);
        }

        return text;
    }

    /**
     * JSON 정제 - 파싱 오류를 일으키는 요소 제거/수정
     */
    private String sanitizeJson(String json) {
        // 1. 제어 문자 제거 (탭/개행 제외)
        json = json.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");

        // 2. JSON 문자열 값 내부의 줄바꿈을 공백으로 변경
        // "question": "이것은
        // 두 줄입니다" -> "question": "이것은 두 줄입니다"
        json = fixMultilineStrings(json);

        // 3. 잘못된 이스케이프 시퀀스 수정
        // \" 뒤에 바로 문자가 오는 경우 등
        json = fixEscapeSequences(json);

        return json;
    }

    /**
     * JSON 문자열 내부의 개행 문자 처리
     */
    private String fixMultilineStrings(String json) {
        // "key": "value with
        // newline" 패턴을 찾아서 개행을 공백으로 치환
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                result.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                result.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                result.append(c);
                continue;
            }

            // 문자열 내부의 개행을 공백으로 변경
            if (inString && (c == '\n' || c == '\r')) {
                result.append(' ');
                continue;
            }

            result.append(c);
        }

        return result.toString();
    }

    /**
     * 이스케이프 시퀀스 수정
     */
    private String fixEscapeSequences(String json) {
        // 문자열 값 내부의 따옴표가 이스케이프되지 않은 경우 수정
        // 정규식으로 "key": "value "with" quotes" 패턴 찾기
        Pattern pattern = Pattern.compile("\"(question|solution|answer|options)\"\\s*:\\s*\"([^\"]*?)\"");
        Matcher matcher = pattern.matcher(json);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);

            // 값 내부의 이스케이프되지 않은 따옴표 처리
            // 이미 \"로 이스케이프된 것은 보존
            String fixedValue = value.replaceAll("(?<!\\\\)\"", "\\\\\"");

            matcher.appendReplacement(sb, "\"" + key + "\": \"" + fixedValue + "\"");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 최후의 수단: 공격적인 JSON 복구
     */
    private String aggressiveJsonRecovery(String json) {
        log.warn("공격적인 JSON 복구 시도 중...");

        // 1. 모든 연속된 공백을 하나로
        json = json.replaceAll("\\s+", " ");

        // 2. 문자열 값 내부의 따옴표를 모두 작은따옴표로 변경
        json = replaceQuotesInStringValues(json);

        // 3. 잘못된 trailing comma 제거
        json = json.replaceAll(",\\s*([}\\]])", "$1");

        return json;
    }

    /**
     * 문자열 값 내부의 따옴표를 작은따옴표로 변경
     */
    private String replaceQuotesInStringValues(String json) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean inKey = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            char prev = i > 0 ? json.charAt(i - 1) : ' ';
            char next = i < json.length() - 1 ? json.charAt(i + 1) : ' ';

            if (escaped) {
                result.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                result.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                // 키-값 구분: "key": 다음 따옴표는 값 시작
                if (!inString && next == ':') {
                    inKey = true;
                    result.append(c);
                    inString = true;
                } else if (!inString && prev == ':') {
                    inKey = false;
                    result.append(c);
                    inString = true;
                } else if (inString && !inKey) {
                    // 값 내부의 따옴표는 작은따옴표로
                    if (next != ',' && next != '}' && next != ']') {
                        result.append('\'');
                    } else {
                        result.append(c);
                        inString = false;
                    }
                } else {
                    result.append(c);
                    inString = !inString;
                }
                continue;
            }

            result.append(c);
        }

        return result.toString();
    }

    /**
     * 1. [맞춤 추천] 프롬프트
     */
    public JsonNode generateRecommendedProblem(ProblemRequestDto prefs) {
        String prompt = String.format("""
            당신은 대한민국 최고 수준의 %s 강사입니다.
            다음 조건에 맞는 문제를 3개 생성하고, 반드시 유효한 JSON 배열로만 응답하세요.
            
            ⚠️ 중요 규칙:
            1. 오직 JSON 배열만 출력 (설명문, 마크다운 코드블록 금지)
            2. 모든 문자열 값은 한 줄로 작성 (줄바꿈 금지)
            3. 문자열 내부의 따옴표는 작은따옴표(')로 대체
            4. LaTeX 수식: 인라인 모드만 사용 ($...$), 백슬래시 1개 (\\frac)
            
            [조건]
            - 학년: %s
            - 과목: %s  
            - 단원: %s
            - 난이도: %s
            - 유형: %s
            
            [출력 예시]
            [
                {
                  "grade": "%s",
                  "subject": "%s",
                  "difficulty": "%s",
                  "tags": ["개념1", "개념2"],
                  "points": 20,
                  "question": "문제 텍스트 (한 줄, 따옴표는 ' 사용)",
                  "options": ["선택지1", "선택지2", "선택지3", "선택지4"],
                  "answer": "정답",
                  "solution": "풀이 과정 (한 줄로 작성)",
                  "isSubjective": false
                },
                {
                  "grade": "%s",
                  "subject": "%s",
                  "difficulty": "%s",
                  "tags": ["개념3"],
                  "points": 25,
                  "question": "두 번째 문제",
                  "options": ["선택지1", "선택지2", "선택지3", "선택지4"],
                  "answer": "정답",
                  "solution": "풀이",
                  "isSubjective": false
                },
                {
                  "grade": "%s",
                  "subject": "%s",
                  "difficulty": "상",
                  "tags": ["심화"],
                  "points": 30,
                  "question": "세 번째 문제",
                  "options": [],
                  "answer": "정답",
                  "solution": "풀이",
                  "isSubjective": true
                }
            ]
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
     * 2. [유사 문제] 프롬프트
     */
    public JsonNode generateSimilarProblem(Problem baseProblem) {
        String prompt = String.format("""
            당신은 수학 문제 변형 전문가입니다.
            아래 원본 문제와 동일한 개념이지만 숫자나 상황이 다른 유사 문제 3개를 생성하세요.
            
            [원본 문제]
            %s
            
            ⚠️ 중요 규칙:
            1. 오직 JSON 배열만 출력
            2. 모든 문자열은 한 줄로 작성
            3. 문자열 내 따옴표는 작은따옴표(')로 대체
            4. LaTeX: 인라인 모드($...$), 백슬래시 1개
            
            [출력 형식]
            [
                {
                  "grade": "%s",
                  "subject": "%s",
                  "difficulty": "%s",
                  "tags": ["유사", "변형"],
                  "points": %d,
                  "question": "변형 문제1 (한 줄)",
                  "options": ["①", "②", "③", "④"],
                  "answer": "정답",
                  "solution": "풀이 (한 줄)",
                  "isSubjective": %s
                },
                {
                  "grade": "%s",
                  "subject": "%s",
                  "difficulty": "%s",
                  "tags": ["유사"],
                  "points": %d,
                  "question": "변형 문제2",
                  "options": ["①", "②", "③", "④"],
                  "answer": "정답",
                  "solution": "풀이",
                  "isSubjective": %s
                },
                {
                  "grade": "%s",
                  "subject": "%s",
                  "difficulty": "%s",
                  "tags": ["유사"],
                  "points": %d,
                  "question": "변형 문제3",
                  "options": ["①", "②", "③", "④"],
                  "answer": "정답",
                  "solution": "풀이",
                  "isSubjective": %s
                }
            ]
            """,
                baseProblem.getQuestion(),
                baseProblem.getGrade(), baseProblem.getSubject(), baseProblem.getDifficulty(),
                baseProblem.getPoints(), baseProblem.isSubjective(),
                baseProblem.getGrade(), baseProblem.getSubject(), baseProblem.getDifficulty(),
                baseProblem.getPoints(), baseProblem.isSubjective(),
                baseProblem.getGrade(), baseProblem.getSubject(), baseProblem.getDifficulty(),
                baseProblem.getPoints(), baseProblem.isSubjective()
        );

        return callGeminiApi(prompt);
    }

    /**
     * 3. [멀티모달] 이미지 분석
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
                        첨부된 이미지를 분석하여 1개의 문제를 JSON 배열로 반환하세요.
                        
                        ⚠️ 중요:
                        1. 오직 JSON 배열만 출력
                        2. 문자열은 한 줄로 작성
                        3. 따옴표는 작은따옴표(')로 대체
                        4. LaTeX: $...$ 형식, 백슬래시 1개
                        
                        [출력]
                        [
                            {
                              "grade": "고1",
                              "subject": "수학",
                              "difficulty": "중",
                              "tags": ["사진분석"],
                              "points": 20,
                              "question": "이미지 문제 (한 줄)",
                              "options": ["①", "②", "③", "④"],
                              "answer": "정답",
                              "solution": "풀이 (한 줄)",
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

            String jsonArray = extractJsonArray(aiText);
            String cleanJson = sanitizeJson(jsonArray);

            return objectMapper.readTree(cleanJson);

        } catch (Exception e) {
            log.error("사진 분석 실패: {}", e.getMessage(), e);
            return null;
        }
    }
}