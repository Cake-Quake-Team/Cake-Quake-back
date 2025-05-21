# cake-quake-back/Dockerfile

### 1. Build stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# 1) Gradle Wrapper 및 설정 복사
COPY gradlew gradlew.bat ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 2) 소스 코드 복사
COPY src src

# 3) Wrapper 실행 권한 부여 및 빌드
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

### 2. Run stage
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 1) 빌드 결과물(JAR)을 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 2) 애플리케이션 포트 노출
EXPOSE 8080

# 3) 엔트리포인트 설정
ENTRYPOINT ["java", "-jar", "app.jar"]
