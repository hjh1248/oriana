package com.oriana.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class AiResponseDto {
    // 1. AI가 읽어낸 문제 원문
    @JsonProperty("original_problem")
    private String originalProblem;

    // 2. 핵심 개념 설명
    @JsonProperty("concept")
    private String concept;

    // 3. 상세 풀이
    @JsonProperty("solution")
    private String solution;

    // 4. 유사 문제 3개 (리스트)
    @JsonProperty("similar_questions")
    private List<PracticeQuestionDto> similarQuestions;

    @Data
    @NoArgsConstructor
    public static class PracticeQuestionDto {
        private String question;   // 문제 지문
        private String answer;     // 정답
        private List<String> options; // 보기 (객관식)
    }
}