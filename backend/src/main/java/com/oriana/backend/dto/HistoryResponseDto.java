package com.oriana.backend.dto;

import com.oriana.backend.domain.SolveHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HistoryResponseDto {
    private Long id;              // 히스토리 ID
    private LocalDateTime solvedAt; // 언제 풀었는지
    private boolean isCorrect;      // 맞았는지 틀렸는지
    private int attemptCount;       // 몇 번 시도했는지
    private ProblemResponseDto problem; // ✨ 문제 원본 데이터 (통째로 넘겨줌!)

    // Entity -> DTO 변환 마법사
    public static HistoryResponseDto from(SolveHistory history) {
        return HistoryResponseDto.builder()
                .id(history.getId())
                .solvedAt(history.getSolvedAt())
                .isCorrect(history.isCorrect())
                .attemptCount(history.getAttemptCount())
                // Problem도 DTO로 변환해서 쏙 넣어줌
                .problem(ProblemResponseDto.from(history.getProblem())) 
                .build();
    }
}