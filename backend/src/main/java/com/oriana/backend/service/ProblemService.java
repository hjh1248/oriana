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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

    private final AiService aiService;
    private final ProblemRepository problemRepository;

    // ✨ 1. 맞춤 추천 문제 3개 생성 & DB 저장
    @Transactional
    public List<ProblemResponseDto> createRecommendedProblem(ProblemRequestDto prefs) {
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

                // 💡 1. 보기(Options) 리스트 추출 - 각 항목 길이 제한
                List<String> optionsList = new ArrayList<>();
                json.path("options").forEach(opt -> {
                    String optText = opt.asText();
                    // 각 선택지는 500자로 제한 (안전)
                    if (optText.length() > 500) {
                        optText = optText.substring(0, 500);
                        log.warn("⚠️ 선택지가 500자를 초과하여 잘랐습니다.");
                    }
                    optionsList.add(optText);
                });

                // 💡 2. 태그(Tags) 리스트 추출 - 각 항목 길이 제한
                List<String> tagsList = new ArrayList<>();
                json.path("tags").forEach(tag -> {
                    String tagText = tag.asText();
                    // 각 태그는 50자로 제한
                    if (tagText.length() > 50) {
                        tagText = tagText.substring(0, 50);
                        log.warn("⚠️ 태그가 50자를 초과하여 잘랐습니다: {}", tagText);
                    }
                    tagsList.add(tagText);
                });

                // 💡 3. 길이 제한이 있는 필드들을 안전하게 추출
                // Entity에서 VARCHAR(255)로 설정된 필드들
                String grade = truncate(json.path("grade").asText("고1"), 255, "학년");
                String subject = truncate(json.path("subject").asText("수학"), 255, "과목");
                String difficulty = truncate(json.path("difficulty").asText("중"), 255, "난이도");

                // TEXT 타입 필드들 (여유있게 설정)
                String question = json.path("question").asText();
                String answer = json.path("answer").asText("");
                String solution = json.path("solution").asText("풀이가 제공되지 않습니다.");

                // 💡 4. Entity 생성
                Problem problem = Problem.builder()
                        .parentProblem(parent)
                        .sourceType(source)
                        .grade(grade)
                        .subject(subject)
                        .difficulty(difficulty)
                        .tags(tagsList)
                        .question(question)
                        .options(optionsList)
                        .answer(answer)
                        .solution(solution)
                        .points(json.path("points").asInt(20))
                        .isSubjective(json.path("isSubjective").asBoolean(false))
                        .build();

                newProblems.add(problem);
                log.info("✅ 문제 생성 완료: {} (태그: {}개, 선택지: {}개)",
                        question.substring(0, Math.min(30, question.length())),
                        tagsList.size(),
                        optionsList.size());

            } catch (Exception e) {
                // 🛡️ [방어 3] 특정 문제 하나가 파싱하다 터져도, 나머지 문제는 살림!
                log.error("❌ 개별 문제 변환 중 오류 발생 (해당 문제만 스킵): {}", e.getMessage(), e);
            }
        }

        // 하나라도 제대로 파싱된 문제가 있다면 저장
        if (!newProblems.isEmpty()) {
            try {
                problemRepository.saveAll(newProblems);
                log.info("✅ 총 {}개의 문제가 DB에 저장되었습니다.", newProblems.size());
            } catch (Exception e) {
                log.error("❌ DB 저장 실패: {}", e.getMessage(), e);
                return new ArrayList<>();
            }
        }

        return newProblems.stream()
                .map(ProblemResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 🛡️ 문자열 길이 제한 유틸리티
     * DB 컬럼 길이를 초과하지 않도록 안전하게 자르기
     */
    private String truncate(String str, int maxLength, String fieldName) {
        if (str == null || str.isEmpty()) {
            return str == null ? "" : str;
        }

        if (str.length() <= maxLength) {
            return str;
        }

        log.warn("⚠️ {}이(가) {}자를 초과하여 잘랐습니다. 원본 길이: {}, 잘린 내용: {}...",
                fieldName, maxLength, str.length(),
                str.substring(0, Math.min(30, str.length())));

        return str.substring(0, maxLength);
    }

    @Transactional(readOnly = true)
    public ProblemResponseDto getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문제를 찾을 수 없어! ID: " + id));

        return ProblemResponseDto.from(problem);
    }

    @Transactional
    public List<ProblemResponseDto> generateSimilarProblems(Long baseId) {
        Problem baseProblem = problemRepository.findById(baseId)
                .orElseThrow(() -> new IllegalArgumentException("원본 문제를 찾을 수 없습니다."));

        JsonNode aiResponseArray = aiService.generateSimilarProblem(baseProblem);
        return saveProblemListToDb(aiResponseArray, ProblemSource.SIMILAR, baseProblem);
    }

    @Transactional(readOnly = true)
    public List<ProblemResponseDto> getSimilarProblems(Long baseId) {
        List<Problem> similarList = problemRepository.findByParentProblemIdOrderByCreatedAtDesc(baseId);

        return similarList.stream()
                .map(ProblemResponseDto::from)
                .collect(Collectors.toList());
    }
}