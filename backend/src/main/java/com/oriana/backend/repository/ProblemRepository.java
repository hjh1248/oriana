package com.oriana.backend.repository;

import com.oriana.backend.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByParentProblemIdOrderByCreatedAtDesc(Long parentId);
}