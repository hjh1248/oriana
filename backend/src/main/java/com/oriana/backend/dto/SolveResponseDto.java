package com.oriana.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SolveResponseDto {
    private boolean isCorrect;     // 정답 여부
    private boolean isRewarded;    // 포인트 지급 여부 (첫 시도 정답일 때만 true)
    private int earnedPoints;      // 이번에 얻은 포인트
    private int totalPoints;       // 현재 총 보유 포인트
    private int currentLevel;      // 현재 레벨 (혹시 레벨업 했을까봐!)
}