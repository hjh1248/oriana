package com.oriana.backend.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "problems")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 💡 원본 문제와 유사 문제를 연결해주는 핵심 족보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_problem_id")
    private Problem parentProblem; 

    // ✨ 문제 생성 출처
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private ProblemSource sourceType;

    @Column(length = 20)
    private String grade; // 예: 고1

    @Column(length = 50)
    private String subject; // 예: 수학(상)

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> tags; // 예: ["다항식", "인수분해"]

    @Column(length = 10)
    private String difficulty; // 상, 중, 하

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question; // 문제 텍스트 (수식 포함)

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> options; // 객관식 보기 리스트

    @Column(length = 50, nullable = false)
    private String answer; // 정답

    @Column(columnDefinition = "TEXT", nullable = false)
    private String solution; // 풀이

    @Column(nullable = false)
    private int points; // 이 문제 풀면 주는 보상 포인트

    @Column(name = "is_subjective", nullable = false)
    private boolean isSubjective; // 주관식 여부

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}