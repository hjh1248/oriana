package com.oriana.backend.config;

import com.oriana.backend.domain.User;
import com.oriana.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DummyDataLoader {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            // 💡 DB에 유저가 한 명도 없을 때만! 테스트용 1번 유저를 자동 생성해 줌
            if (userRepository.count() == 0) {
                User dummyUser = User.builder()
                        .name("김오리") // 네 이름으로 박아줄게! 😎
                        .points(0)
                        .level(1)
                        .build();
                userRepository.save(dummyUser);
                System.out.println("✨ 테스트용 1번 유저(김오리)가 DB에 성공적으로 생성되었습니다!");
            }
        };
    }
}