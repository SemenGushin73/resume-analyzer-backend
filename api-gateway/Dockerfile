FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

ENV GRADLE_USER_HOME=/app/.gradle
RUN mkdir -p $GRADLE_USER_HOME && chmod -R 777 $GRADLE_USER_HOME

COPY gradlew gradlew
COPY gradle ./gradle
COPY settings.gradle .
COPY build.gradle .

COPY lib ./lib
COPY api-gateway ./api-gateway

RUN ./gradlew :lib:publishToMavenLocal --no-daemon

RUN ./gradlew :api-gateway:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/api-gateway/build/libs/api-gateway-*.jar /app/api-gateway.jar

ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]

EXPOSE 8765
