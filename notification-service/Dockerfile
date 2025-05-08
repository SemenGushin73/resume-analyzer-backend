FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Скачиваем OpenTelemetry Java Agent (если нужно)
RUN wget -O /opentelemetry-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# Копируем JAR-файл notification-сервиса (путь зависит от твоей структуры)
COPY notification-service/build/libs/notification-service-*.jar ./app.jar

ENTRYPOINT [ "java", "-javaagent:/opentelemetry-javaagent.jar", "-jar", "/app/app.jar" ]

EXPOSE 8885