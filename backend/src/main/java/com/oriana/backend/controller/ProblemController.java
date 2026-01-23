package com.oriana.backend.controller;

import com.oriana.backend.dto.ProblemRequestDto;
import com.oriana.backend.dto.ProblemResponseDto;
import com.oriana.backend.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Vue 프론트엔드와 통신 허용 (CORS)
public class ProblemController {

    private final ProblemService problemService;

    // 1. 맞춤 문제 추천 API (POST /api/problems/recommend)
    @PostMapping("/recommend")
    public ResponseEntity<ProblemResponseDto> recommendProblem(@RequestBody ProblemRequestDto prefs) {
        return ResponseEntity.ok(problemService.createRecommendedProblem(prefs));
    }

    // 2. 사진 문제 스캔 API (POST /api/problems/upload)
    @PostMapping("/upload")
    public ResponseEntity<ProblemResponseDto> uploadProblem(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(problemService.createPhotoProblem(file));
    }

    // 3. 유사 문제 변형 API (GET /api/problems/similar?baseId=101)
    @GetMapping("/similar")
    public ResponseEntity<ProblemResponseDto> getSimilarProblem(@RequestParam Long baseId) {
        return ResponseEntity.ok(problemService.createSimilarProblem(baseId));
    }
}