package com.oriana.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WrongAnswer extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 1. 원본 이미지 경로 (S3 URL 등)
    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    // 2. AI가 분석한 원본 문제 텍스트 (OCR 결과)
    @Column(columnDefinition = "TEXT")
    private String originalQuestionText;

    // 3. AI가 생성한 핵심 개념 설명
    @Column(columnDefinition = "TEXT")
    private String conceptExplanation;

    // 4. AI가 생성한 상세 풀이
    @Column(columnDefinition = "TEXT")
    private String solutionExplanation;

    // 5. 유사 문제들 (1:N 관계)
    @OneToMany(mappedBy = "wrongAnswer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PracticeQuestion> practiceQuestions = new ArrayList<>();
}