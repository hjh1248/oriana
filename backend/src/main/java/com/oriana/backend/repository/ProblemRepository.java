package com.oriana.backend.repository;

import com.oriana.backend.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    // 나중에 특정 조건(학년, 난이도 등)으로 문제를 검색할 때 커스텀 메소드를 추가할 공간이야!
}