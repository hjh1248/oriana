package com.oriana.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final AiService aiService;
    private final ProblemRepository problemRepository;

    // ✨ 1. 맞춤 추천 문제 3개 생성 & DB 저장
    @Transactional
    public List<ProblemResponseDto> createRecommendedProblem(ProblemRequestDto prefs) {
        // AI가 3개짜리 JSON 배열을 리턴함
        JsonNode aiResponseArray = aiService.generateRecommendedProblem(prefs);
        return saveProblemListToDb(aiResponseArray, ProblemSource.RECOMMEND, null);
    }

    // ✨ 2. 사진 스캔 문제 3개 생성 & DB 저장
    @Transactional
    public List<ProblemResponseDto> createPhotoProblem(MultipartFile imageFile) {
        JsonNode aiResponseArray = aiService.analyzePhotoDirectly(imageFile);
        return saveProblemListToDb(aiResponseArray, ProblemSource.PHOTO, null);
    }

    // ✨ 3. 유사 문제 3개 생성 & DB 저장 (족보 연결)
    @Transactional
    public List<ProblemResponseDto> createSimilarProblem(Long baseId) {
        Problem baseProblem = problemRepository.findById(baseId)
                .orElseThrow(() -> new IllegalArgumentException("원본 문제를 찾을 수 없습니다."));

        JsonNode aiResponseArray = aiService.generateSimilarProblem(baseProblem);
        return saveProblemListToDb(aiResponseArray, ProblemSource.SIMILAR, baseProblem);
    }

    // 🛠️ 핵심 공통 로직: AI가 준 JSON 배열(3개)을 Entity로 변환하고 DB에 저장
    private List<ProblemResponseDto> saveProblemListToDb(JsonNode jsonArray, ProblemSource source, Problem parent) {
        List<Problem> newProblems = new ArrayList<>();

        // JSON 배열을 돌면서 각각의 문제 데이터를 추출
        for (JsonNode json : jsonArray) {

            // 💡 1. 보기(Options) 리스트 추출
            List<String> optionsList = new ArrayList<>();
            json.path("options").forEach(opt -> optionsList.add(opt.asText()));

            // 💡 2. 태그(Tags) 리스트 추출
            List<String> tagsList = new ArrayList<>();
            json.path("tags").forEach(tag -> tagsList.add(tag.asText()));

            // 💡 3. AI가 준 메타데이터를 그대로 사용해서 Entity 생성!
            Problem problem = Problem.builder()
                    .parentProblem(parent) // 유사 문제일 때만 원본 ID가 연결됨
                    .sourceType(source)
                    .grade(json.path("grade").asText())           // AI가 판단한 학년
                    .subject(json.path("subject").asText())       // AI가 판단한 과목
                    .difficulty(json.path("difficulty").asText()) // AI가 조절한 난이도
                    .tags(tagsList)                               // AI가 추출한 핵심 개념 태그들!
                    .question(json.path("question").asText())
                    .options(optionsList)
                    .answer(json.path("answer").asText())
                    .solution(json.path("solution").asText())
                    .points(json.path("points").asInt())          // AI가 책정한 보상 포인트!
                    .isSubjective(json.path("isSubjective").asBoolean())
                    .build();

            newProblems.add(problem);
        }

        // DB에 3개 한 번에 촥! 저장 (성능 최적화)
        problemRepository.saveAll(newProblems);

        // 프론트엔드로 보낼 DTO 리스트로 변환해서 리턴
        return newProblems.stream()
                .map(ProblemResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProblemResponseDto getProblemById(Long id) {
        // 1. DB에서 ID로 조회, 없으면 404 느낌의 예외 던지기
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없어! ID: " + id));

        // 2. Entity를 프론트가 쓰기 좋은 DTO로 변환해서 리턴
        return ProblemResponseDto.from(problem);
    }

    @Transactional
    public List<ProblemResponseDto> generateSimilarProblems(Long baseId) {
        Problem baseProblem = problemRepository.findById(baseId)
                .orElseThrow(() -> new IllegalArgumentException("원본 문제를 찾을 수 없습니다."));

        // AI를 통해 유사 문제 3개 생성
        JsonNode aiResponseArray = aiService.generateSimilarProblem(baseProblem);

        // DB 저장 후 DTO로 반환
        return saveProblemListToDb(aiResponseArray, ProblemSource.SIMILAR, baseProblem);
    }

    // 📋 [조회] 이미 생성된 유사 문제 리스트 가져오기 (GET 요청 시 사용)
    @Transactional(readOnly = true)
    public List<ProblemResponseDto> getSimilarProblems(Long baseId) {
        // DB에서 해당 baseId를 부모로 가진 문제들을 싹 긁어와
        List<Problem> similarList = problemRepository.findByParentProblemIdOrderByCreatedAtDesc(baseId);

        return similarList.stream()
                .map(ProblemResponseDto::from)
                .collect(Collectors.toList());
    }
}