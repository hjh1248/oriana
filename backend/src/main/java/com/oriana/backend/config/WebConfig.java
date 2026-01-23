package com.oriana.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "http://52.79.210.0",           // ✅ 프론트엔드 외부 접속
                        "http://52.79.210.0:8080",      // ✅ 백엔드 외부 접속
                        "http://52.79.210.0:5173",      // ✅ 프론트엔드 포트까지
                        "http://frontend",              // ✅ 도커 서비스명 (docker-compose 사용 시)
                        "http://backend"                // ✅ 도커 서비스명
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}