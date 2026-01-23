package com.oriana.backend.dto;

import com.oriana.backend.domain.Problem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProblemResponseDto {
    private Long id;
    private String grade;
    private String subject;
    private List<String> tags;
    private String difficulty;
    private String question;
    private List<String> options;
    private String answer;
    private String solution;
    private int points;
    private boolean isSubjective;

    // Entity -> DTO 변환을 쉽게 해주는 마법사
    public static ProblemResponseDto from(Problem problem) {
        return ProblemResponseDto.builder()
                .id(problem.getId())
                .grade(problem.getGrade())
                .subject(problem.getSubject())
                .tags(problem.getTags())
                .difficulty(problem.getDifficulty())
                .question(problem.getQuestion())
                .options(problem.getOptions())
                .answer(problem.getAnswer())
                .solution(problem.getSolution())
                .points(problem.getPoints())
                .isSubjective(problem.isSubjective())
                .build();
    }
}