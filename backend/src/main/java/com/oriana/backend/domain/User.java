package com.oriana.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    @ColumnDefault("1")
    @Builder.Default
    private int level = 1; // 초기 레벨 1

    @Column(nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private int points = 0; // 현재 보유 포인트

    @Column(name = "next_level_points", nullable = false)
    @ColumnDefault("500")
    @Builder.Default
    private int nextLevelPoints = 500; // 다음 레벨업에 필요한 포인트

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 포인트 획득 및 레벨업 비즈니스 로직
    public void addPoints(int earnedPoints) {
        this.points += earnedPoints;
        if (this.points >= this.nextLevelPoints) {
            this.level += 1;
            this.nextLevelPoints += 500; // 레벨업 시 다음 기준치 증가
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}