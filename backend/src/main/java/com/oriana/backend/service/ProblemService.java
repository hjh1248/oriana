package com.oriana.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.oriana.backend.domain.Problem;
import com.oriana.backend.domain.ProblemSource;
import com.oriana.backend.dto.ProblemRequestDto;
import com.oriana.backend.dto.ProblemResponseDto;
import com.oriana.backend.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
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

        // 🛡️ [방어 1] AI 응답 자체가 null이거나 배열이 아니면 빈 리스트 반환
        if (jsonArray == null || !jsonArray.isArray()) {
            log.error("🚨 AI 응답이 비어있거나 형식이 올바르지 않아 저장을 건너뜁니다.");
            return new ArrayList<>();
        }

        // JSON 배열을 돌면서 각각의 문제 데이터를 추출
        for (JsonNode json : jsonArray) {
            try {
                // 🛡️ [방어 2] 필수 필드(문제 텍스트)가 없으면 이 문제는 스킵
                if (json.path("question").isMissingNode() || json.path("question").asText().isEmpty()) {
                    log.warn("⚠️ 필수 데이터(질문)가 없는 문제가 있어 스킵합니다.");
                    continue;
                }

                // 💡 1. 보기(Options) 리스트 추출
                List<String> optionsList = new ArrayList<>();
                json.path("options").forEach(opt -> optionsList.add(opt.asText()));

                // 💡 2. 태그(Tags) 리스트 추출
                List<String> tagsList = new ArrayList<>();
                json.path("tags").forEach(tag -> tagsList.add(tag.asText()));

                // 💡 3. Entity 생성
                Problem problem = Problem.builder()
                        .parentProblem(parent)
                        .sourceType(source)
                        .grade(json.path("grade").asText("고1")) // 기본값 설정
                        .subject(json.path("subject").asText("수학"))
                        .difficulty(json.path("difficulty").asText("중"))
                        .tags(tagsList)
                        .question(json.path("question").asText())
                        .options(optionsList)
                        .answer(json.path("answer").asText())
                        .solution(json.path("solution").asText("풀이가 제공되지 않습니다."))
                        .points(json.path("points").asInt(20))
                        .isSubjective(json.path("isSubjective").asBoolean(false))
                        .build();

                newProblems.add(problem);

            } catch (Exception e) {
                // 🛡️ [방어 3] 특정 문제 하나가 파싱하다 터져도, 나머지 문제는 살림!
                log.error("❌ 개별 문제 변환 중 오류 발생 (해당 문제만 스킵): {}", e.getMessage());
            }
        }

        // 하나라도 제대로 파싱된 문제가 있다면 저장
        if (!newProblems.isEmpty()) {
            problemRepository.saveAll(newProblems);
        }

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