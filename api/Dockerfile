FROM openjdk:17-jdk-slim

WORKDIR /app

# 멀티모듈이므로 루트 전체 복사
COPY . .

RUN apt-get update && apt-get install -y default-mysql-client

# 웹 모듈만 빌드
RUN ./gradlew :api:bootJar --no-daemon

# 결과 jar 복사
RUN cp api/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
