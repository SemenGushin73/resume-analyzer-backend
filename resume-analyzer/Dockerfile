FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

ENV GRADLE_USER_HOME=/app/.gradle
RUN mkdir -p $GRADLE_USER_HOME && chmod -R 777 $GRADLE_USER_HOME

COPY gradlew ./
COPY gradle/wrapper gradle/wrapper
COPY build.gradle settings.gradle ./
COPY lib ./lib
COPY resume-analyzer ./resume-analyzer

RUN ./gradlew :lib:publishToMavenLocal --no-daemon

RUN ./gradlew :resume-analyzer:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/resume-analyzer/build/libs/resume-analyzer-*.jar /app/resume-analyzer.jar

ENTRYPOINT ["java", "-jar", "resume-analyzer.jar"]

EXPOSE 8083