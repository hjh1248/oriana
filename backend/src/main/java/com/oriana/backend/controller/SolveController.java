package com.oriana.backend.controller;

import com.oriana.backend.dto.SolveRequestDto;
import com.oriana.backend.dto.SolveResponseDto;
import com.oriana.backend.service.SolveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.oriana.backend.dto.HistoryResponseDto;
import java.util.List;

@RestController
@RequestMapping("/api/solve")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class SolveController {

    private final SolveService solveService;

    // 문제 채점 및 제출 API (POST /api/solve)
    @PostMapping
    public ResponseEntity<SolveResponseDto> submitAnswer(@RequestBody SolveRequestDto request) {
        return ResponseEntity.ok(solveService.gradeProblem(request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<HistoryResponseDto>> getHistory(@RequestParam Long userId) {
        return ResponseEntity.ok(solveService.getUserHistory(userId));
    }
}