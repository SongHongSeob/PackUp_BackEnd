# 1. Java 17 이미지를 베이스로 사용
FROM openjdk:17

# 2. jar 파일 경로 설정 (Gradle 빌드 기준: build/libs/)
ARG JAR_FILE=build/libs/*.jar

# 3. jar 파일을 컨테이너 안으로 복사
COPY ${JAR_FILE} app.jar

# 4. jar 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]

# 5. JVM 타임존
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar"]