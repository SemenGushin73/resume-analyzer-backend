# 1. Этап сборки
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Кэширование Gradle (ускоряет сборку)
ENV GRADLE_USER_HOME=/app/.gradle
RUN mkdir -p $GRADLE_USER_HOME && chmod -R 777 $GRADLE_USER_HOME

# Копируем файлы Gradle перед кодом, чтобы кешировать зависимости
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .


# Копируем код после кеширования зависимостей
COPY src ./src

# Собираем JAR
RUN ./gradlew clean build --no-daemon

# 2. Минимальный финальный образ
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем собранный JAR файл из предыдущего шага
COPY --from=build /app/build/libs/resume-generator-*.jar /app/resume-generator.jar

# Указываем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "resume-generator.jar"]

# Открываем порт 8080
EXPOSE 8082