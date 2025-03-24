# Используем официальный образ OpenJDK для Java 21
FROM openjdk:21-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл (имя меняйте под свой сервис)
COPY build/libs/kafka-dto-1.0.1.jar /app/kafka.jar


# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/kafka.jar"]

