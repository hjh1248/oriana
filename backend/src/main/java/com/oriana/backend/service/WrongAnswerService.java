package com.oriana.backend.service;

import com.oriana.backend.domain.Member;
import com.oriana.backend.domain.PracticeQuestion;
import com.oriana.backend.domain.WrongAnswer;
import com.oriana.backend.dto.AiResponseDto;
import com.oriana.backend.repository.MemberRepository;
import com.oriana.backend.repository.WrongAnswerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class WrongAnswerService {

    private final AiService aiService;
    private final WrongAnswerRepository wrongAnswerRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public AiResponseDto processAndSaveWrongAnswer(MultipartFile image, String userEmail) throws IOException {
        // 1. 사용자 찾기 (없으면 에러, 해커톤용 임시)
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. AI한테 이미지 분석 요청 (여기서 시간 좀 걸림)
        AiResponseDto aiResult = aiService.analyzeImage(image);

        // 3. 오답노트(WrongAnswer) 엔티티 생성
        WrongAnswer wrongAnswer = WrongAnswer.builder()
                .member(member)
                .imageUrl("temp_image_url") // S3 연결 전이라 임시 텍스트 저장
                .originalQuestionText(aiResult.getOriginalProblem())
                .conceptExplanation(aiResult.getConcept())
                .solutionExplanation(aiResult.getSolution())
                .build();

        // 4. 유사 문제(PracticeQuestion) 엔티티 생성 및 연결
        for (AiResponseDto.PracticeQuestionDto qDto : aiResult.getSimilarQuestions()) {
            PracticeQuestion practiceQuestion = PracticeQuestion.builder()
                    .wrongAnswer(wrongAnswer) // 연관관계 설정
                    .questionText(qDto.getQuestion())
                    .options(qDto.getOptions().toString()) // 리스트를 문자열로 변환
                    .correctAnswer(qDto.getAnswer())
                    .isCorrect(false) // 아직 안 풀었으니까 기본값 false
                    .build();
            
            wrongAnswer.getPracticeQuestions().add(practiceQuestion);
        }

        // 5. DB에 저장 (Cascade 설정 덕분에 wrongAnswer만 저장하면 유사문제도 자동 저장됨)
        wrongAnswerRepository.save(wrongAnswer);

        // 6. 프론트엔드에 결과 반환
        return aiResult;
    }
}