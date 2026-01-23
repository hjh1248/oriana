package com.oriana.backend.controller;

import com.oriana.backend.dto.ProblemRequestDto;
import com.oriana.backend.dto.ProblemResponseDto;
import com.oriana.backend.dto.SimilarGenerateRequestDto;
import com.oriana.backend.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ProblemController {

    private final ProblemService problemService;

    // 1. 맞춤 문제 추천 API (POST /api/problems/recommend)
    @PostMapping("/recommend")
    public ResponseEntity<List<ProblemResponseDto>> recommendProblem(@RequestBody ProblemRequestDto prefs) {
        return ResponseEntity.ok(problemService.createRecommendedProblem(prefs));
    }

    // 2. 사진 문제 스캔 API (POST /api/problems/upload)
    @PostMapping("/upload")
    public ResponseEntity<List<ProblemResponseDto>> uploadProblem(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(problemService.createPhotoProblem(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemResponseDto> getProblem(@PathVariable Long id) {
        // Service에서 DTO로 변환된 데이터를 받아와서 리턴
        return ResponseEntity.ok(problemService.getProblemById(id));
    }

    // 🔄 유사 문제 생성 API (역할: 데이터 생성 및 저장)
    // 프론트의 api.post('/problems/similar/generate')와 매칭돼!
    @PostMapping("/similar/generate")
    public ResponseEntity<List<ProblemResponseDto>> generateSimilar(@RequestBody SimilarGenerateRequestDto request) {
        return ResponseEntity.ok(problemService.generateSimilarProblems(request.baseId()));
    }

    // 📋 유사 문제 리스트 조회 API (역할: 순수 조회)
    // 프론트의 api.get('/problems/similar')와 매칭돼!
    @GetMapping("/similar")
    public ResponseEntity<List<ProblemResponseDto>> getSimilarList(@RequestParam Long baseId) {
        return ResponseEntity.ok(problemService.getSimilarProblems(baseId));
    }

}