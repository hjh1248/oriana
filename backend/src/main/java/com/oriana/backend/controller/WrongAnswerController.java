package com.oriana.backend.controller;

import com.oriana.backend.dto.AiResponseDto;
import com.oriana.backend.service.WrongAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/wrong-answers")
@RequiredArgsConstructor
public class WrongAnswerController {

    private final WrongAnswerService wrongAnswerService;

    // POST http://localhost:8080/api/wrong-answers/analyze
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AiResponseDto> analyzeWrongAnswer(
            @RequestParam("image") MultipartFile image) throws IOException {
        
        // 해커톤이니까 로그인 구현 건너뛰고, 테스트용 계정 이메일 고정!
        String dummyEmail = "test@ssafy.com";

        AiResponseDto response = wrongAnswerService.processAndSaveWrongAnswer(image, dummyEmail);
        
        return ResponseEntity.ok(response);
    }
}