package com.oriana.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "solve_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SolveHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Column(name = "attempt_count", nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private int attemptCount = 0; // 이 문제를 몇 번 만에 맞췄는지 기록

    @Column(name = "solved_at")
    private LocalDateTime solvedAt;

    // 시도 횟수 증가 로직
    public void incrementAttempt() {
        this.attemptCount++;
    }

    @PrePersist
    protected void onCreate() {
        this.solvedAt = LocalDateTime.now();
    }
}