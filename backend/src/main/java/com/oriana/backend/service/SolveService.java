package com.oriana.backend.service;

import com.oriana.backend.domain.Problem;
import com.oriana.backend.domain.SolveHistory;
import com.oriana.backend.domain.User;
import com.oriana.backend.dto.SolveRequestDto;
import com.oriana.backend.dto.SolveResponseDto;
import com.oriana.backend.repository.ProblemRepository;
import com.oriana.backend.repository.SolveHistoryRepository;
import com.oriana.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oriana.backend.dto.HistoryResponseDto;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolveService {

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final SolveHistoryRepository solveHistoryRepository;

    @Transactional
    public SolveResponseDto gradeProblem(SolveRequestDto request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));

        // 1. 유저의 답과 실제 정답 비교 (공백 제거 후 비교해서 억울하게 틀리는 일 방지!)
        String cleanUserAns = request.getUserAnswer().replaceAll("\\s+", "");
        String cleanRealAns = problem.getAnswer().replaceAll("\\s+", "");
        System.out.println(cleanUserAns);
        System.out.println(cleanRealAns);
        boolean isCorrect = cleanUserAns.equalsIgnoreCase(cleanRealAns);
        System.out.println(isCorrect);

        // 2. 이 유저가 이 문제를 푼 적이 있는지 확인 (없으면 새로 생성)
        SolveHistory history = solveHistoryRepository.findByUserIdAndProblemId(user.getId(), problem.getId())
                .orElse(SolveHistory.builder()
                        .user(user)
                        .problem(problem)
                        .isCorrect(false)
                        .attemptCount(0)
                        .build());

        // 시도 횟수 1 증가
        history.incrementAttempt();
        boolean isFirstTry = (history.getAttemptCount() == 1);
        boolean isRewarded = false;
        int earnedPoints = 0;

        // 3. 정답이고, 첫 번째 시도라면? 포인트 지급! 🎉
        if (isCorrect) {
            history.markAsCorrect(); // 히스토리에도 정답 처리

            if (isFirstTry) {
                earnedPoints = problem.getPoints(); // 문제에 걸린 포인트만큼!
                user.addPoints(earnedPoints);       // 유저 포인트 적립 (레벨업 로직 자동 실행됨)
                isRewarded = true;
            }
        }

        // DB에 변경사항 저장
        solveHistoryRepository.save(history);
        userRepository.save(user);

        // 4. 결과 포장해서 리턴
        return SolveResponseDto.builder()
                .isCorrect(isCorrect)
                .isRewarded(isRewarded)
                .earnedPoints(earnedPoints)
                .totalPoints(user.getPoints())
                .currentLevel(user.getLevel())
                .build();
    }
    @Transactional(readOnly = true)
    public List<HistoryResponseDto> getUserHistory(Long userId) {
        // Repository에서 최신순으로 가져옴
        List<SolveHistory> histories = solveHistoryRepository.findByUserIdOrderBySolvedAtDesc(userId);

        // 프론트엔드가 먹기 좋게 DTO로 변환해서 리턴!
        return histories.stream()
                .map(HistoryResponseDto::from)
                .collect(Collectors.toList());
    }
}