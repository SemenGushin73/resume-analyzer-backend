# Используем официальный образ OpenJDK для Java 21
FROM openjdk:21-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл (имя меняйте под свой сервис)
COPY build/libs/deepseek-service-0.0.1-SNAPSHOT.jar /app/deepseek.jar



# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/deepseek.jar"]

EXPOSE 9090

