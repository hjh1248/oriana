package com.oriana.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriana.backend.domain.Problem;
import com.oriana.backend.domain.ProblemSource;
import com.oriana.backend.dto.ProblemRequestDto;
import com.oriana.backend.dto.ProblemResponseDto;
import com.oriana.backend.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final AiService aiService;
    private final ProblemRepository problemRepository;
    private final ObjectMapper objectMapper;

    // ✨ 1. 맞춤 추천 문제 생성 & DB 저장
    @Transactional
    public ProblemResponseDto createRecommendedProblem(ProblemRequestDto prefs) {
        JsonNode aiResponse = aiService.generateRecommendedProblem(prefs);
        return saveProblemToDb(aiResponse, ProblemSource.RECOMMEND, null);
    }

    // ✨ 2. 사진 스캔 문제 생성 & DB 저장
    @Transactional
    public ProblemResponseDto createPhotoProblem(MultipartFile imageFile) {
        JsonNode aiResponse = aiService.analyzePhotoDirectly(imageFile);
        return saveProblemToDb(aiResponse, ProblemSource.PHOTO, null);
    }

    // ✨ 3. 유사 문제(오답노트용) 생성 & DB 저장 (족보 연결)
    @Transactional
    public ProblemResponseDto createSimilarProblem(Long baseId) {
        Problem baseProblem = problemRepository.findById(baseId)
                .orElseThrow(() -> new IllegalArgumentException("원본 문제를 찾을 수 없습니다."));

        JsonNode aiResponse = aiService.generateSimilarProblem(baseProblem);
        return saveProblemToDb(aiResponse, ProblemSource.SIMILAR, baseProblem);
    }

    // 🛠️ 공통 로직: AI가 준 JSON을 Entity로 바꿔서 DB에 저장하는 함수
    private ProblemResponseDto saveProblemToDb(JsonNode json, ProblemSource source, Problem parent) {
        // options 배열을 List<String>으로 변환
        List<String> optionsList = new ArrayList<>();
        json.path("options").forEach(opt -> optionsList.add(opt.asText()));

        // Problem Entity 생성
        Problem newProblem = Problem.builder()
                .parentProblem(parent) // 유사 문제일 때만 원본 ID가 들어감
                .sourceType(source)
                .grade(json.path("grade").asText("기본값")) // AI가 학년/과목도 채워줌
                .subject(json.path("subject").asText("기본값"))
                .difficulty(json.path("difficulty").asText("중"))
                .question(json.path("question").asText())
                .options(optionsList)
                .answer(json.path("answer").asText())
                .solution(json.path("solution").asText())
                .points(15) // 기본 보상 포인트 (나중에 난이도별 차등 가능)
                .isSubjective(json.path("isSubjective").asBoolean())
                .build();

        // DB에 저장 후, 프론트용 DTO로 변환해서 반환
        problemRepository.save(newProblem);
        return ProblemResponseDto.from(newProblem);
    }
}