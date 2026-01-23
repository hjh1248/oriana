package com.oriana.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProblemRequestDto {
    private String grade;       // 예: "고1"
    private String subject;     // 예: "수학(상)"
    private String unit;        // 예: "다항식"
    private String difficulty;  // 예: "중"
    private String type;        // 예: "객관식", "주관식", "혼합"
}