package com.oriana.backend.repository;

import com.oriana.backend.domain.SolveHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SolveHistoryRepository extends JpaRepository<SolveHistory, Long> {
    // 마이페이지용: 특정 유저의 오답노트(문제 풀이 기록)를 최신순으로 가져오기
    List<SolveHistory> findByUserIdOrderBySolvedAtDesc(Long userId);
    Optional<SolveHistory> findByUserIdAndProblemId(Long userId, Long problemId);
}
