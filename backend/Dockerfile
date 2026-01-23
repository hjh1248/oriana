# 1. 빌드 환경 (Gradle)
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .

# gradlew에 실행 권한 부여하기
RUN chmod +x ./gradlew
# 테스트 제외하고 빌드 (시간/메모리 절약)
RUN ./gradlew bootJar -x test

# 2. 실행 환경 (가벼운 Java)
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
# 빌드된 jar 파일만 가져오기
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]