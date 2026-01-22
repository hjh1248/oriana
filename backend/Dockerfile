# Build Stage
FROM gradle:8-jdk17-alpine AS builder
WORKDIR /build
COPY . .
RUN ./gradlew build -x test --parallel

# Run Stage
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /build/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]