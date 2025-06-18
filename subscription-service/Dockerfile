FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Скачиваем OpenTelemetry Java Agent
RUN wget -O /opentelemetry-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# Копируем готовый JAR (собранный локально)
COPY ./subscription-service/build/libs/subscription-service.jar ./app.jar

ENTRYPOINT ["java", "-javaagent:/opentelemetry-javaagent.jar", "-Dspring.profiles.active=docker", "-jar", "/app/app.jar"]

EXPOSE 8086
