package com.oriana.backend.service;

import com.oriana.backend.domain.User;
import com.oriana.backend.dto.UserResponseDto;
import com.oriana.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 데이터 조회만 하니까 readOnly로 최적화!
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto findById(Long id) {
        // 1. DB에서 1번 유저 찾기 (없으면 에러)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없어! id = " + id));

        // 2. 엔티티를 프론트엔드용 DTO로 변환해서 넘겨줌
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .level(user.getLevel())
                .points(user.getPoints())
                .nextLevelPoints(user.getNextLevelPoints())
                .build();
    }
}