package com.oriana.backend.repository;

import com.oriana.backend.domain.WrongAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WrongAnswerRepository extends JpaRepository<WrongAnswer, Long> {
    // 나중에 '내 오답노트 조회' 기능 만들 때 필요함
    // List<WrongAnswer> findByMemberId(Long memberId);
}