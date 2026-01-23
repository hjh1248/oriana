package com.oriana.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SolveRequestDto {
    private Long userId;     // 누가 풀었는지
    private Long problemId;  // 몇 번 문제를 풀었는지
    private String userAnswer; // 유저가 적어낸 답
}