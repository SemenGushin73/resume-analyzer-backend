# Используем официальный образ OpenJDK для Java 21
FROM openjdk:21-jdk-slim as build

# Устанавливаем рабочую директорию в контейнере
WORKDIR /app

# Кэширование Gradle
ENV GRADLE_USER_HOME=/app/.gradle
RUN mkdir -p /app/.gradle && chmod -R 777 /app/.gradle

# Копируем файл gradle wrapper и build.gradle в рабочую директорию
COPY gradlew .
COPY gradle ./gradle
COPY gradle.properties .
COPY build.gradle .
COPY settings.gradle .

# Копируем исходный код приложения
COPY src ./src
COPY kafka-dto ./kafka-dto

# Строим приложение
RUN ./gradlew clean build --no-daemon

# Используем минимальный образ OpenJDK для продакшн
FROM openjdk:21-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR файл из предыдущего шага
# COPY --from=build /app/build/libs/resume-generator-0.0.1-SNAPSHOT.jar /app/resume-generator.jar

# Указываем команду для запуска приложения
# ENTRYPOINT ["java", "-jar", "resume-generator.jar"]

# Открываем порт 8080
EXPOSE 8080
