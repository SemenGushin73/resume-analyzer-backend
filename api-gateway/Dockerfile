FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Скачиваем OpenTelemetry Java Agent
RUN wget -O /opentelemetry-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# Копируем готовый JAR (собранный локально)
COPY api-gateway/build/libs/api-gateway-*.jar ./app.jar

ENTRYPOINT [ "java", "-javaagent:/opentelemetry-javaagent.jar", "-jar", "/app/app.jar" ]

EXPOSE 8765
