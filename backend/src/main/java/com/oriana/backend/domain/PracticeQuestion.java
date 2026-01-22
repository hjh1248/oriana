package com.oriana.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PracticeQuestion extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wrong_answer_id")
    private WrongAnswer wrongAnswer;

    // 문제 지문
    @Column(columnDefinition = "TEXT")
    private String questionText;

    // 보기 (객관식일 경우 JSON String으로 저장 추천. 예: ["1.사과", "2.배"])
    // 해커톤에선 편의상 String으로 박고 프론트가 파싱하게 해도 됨
    @Column(columnDefinition = "TEXT")
    private String options; 

    // 정답
    private String correctAnswer;

    // 학생이 선택한 답 (채점용)
    private String userAnswer;

    // 정답 여부 (true: 맞음, false: 틀림)
    private Boolean isCorrect;
}