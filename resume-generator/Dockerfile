FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

ENV GRADLE_USER_HOME=/app/.gradle
RUN mkdir -p $GRADLE_USER_HOME && chmod -R 777 $GRADLE_USER_HOME

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY lib ./lib
COPY resume-generator ./resume-generator

RUN wget -O /tmp/opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

RUN ./gradlew :lib:publishToMavenLocal --no-daemon

RUN ./gradlew :resume-generator:bootJar --no-daemon


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/resume-generator/build/libs/resume-generator-*.jar /app/resume-generator.jar

COPY --from=build /tmp/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar

ENTRYPOINT [ "java", "-javaagent:/opentelemetry-javaagent.jar", "-jar", "/app/resume-generator.jar" ]

EXPOSE 8082
