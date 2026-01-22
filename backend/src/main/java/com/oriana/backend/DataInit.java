package com.oriana.backend;

import com.oriana.backend.domain.Member;
import com.oriana.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final MemberRepository memberRepository;

    @Override
    public void run(String... args) throws Exception {
        // DB에 테스트 계정이 없으면 하나 만듦
        if (memberRepository.findByEmail("test@ssafy.com").isEmpty()) {
            memberRepository.save(Member.builder()
                    .email("test@ssafy.com")
                    .name("해커톤우승자")
                    .password("1234")
                    .build());
            System.out.println("✅ 테스트용 계정 생성 완료: test@ssafy.com");
        }
    }
}