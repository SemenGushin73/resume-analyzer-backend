FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

ENV GRADLE_USER_HOME=/app/.gradle
RUN mkdir -p $GRADLE_USER_HOME && chmod -R 777 $GRADLE_USER_HOME

COPY gradlew .
COPY gradle gradle
COPY gradle/wrapper gradle/wrapper
COPY build.gradle settings.gradle ./
COPY lib ./lib
COPY deepseek-service ./deepseek-service

RUN ./gradlew :lib:publishToMavenLocal --no-daemon

RUN ./gradlew :deepseek-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/deepseek-service/build/libs/deepseek-service-*.jar /app/deepseek.jar

ENTRYPOINT ["java", "-jar", "/app/deepseek.jar"]

EXPOSE 9090