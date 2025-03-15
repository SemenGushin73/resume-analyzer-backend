# Resume Analyzer Backend

## **Описание проекта**
Resume Analyzer Backend — это набор микросервисов, которые реализуют логику анализа резюме, генерации шаблонов и маршрутизации запросов.

## **Технологический стек**
- **Язык**: Java 21
- **Сборщик**: Gradle
- **Фреймворки**:
    - Spring Boot (REST API, Data JPA, Security)
    - Spring Cloud (Eureka, Gateway)
- **Базы данных**:
    - PostgreSQL (основная база данных)
    - MongoDB (хранение шаблонов резюме)
- **Очереди сообщений**: Apache Kafka
- **Документация API**: Springdoc OpenAPI
- **Контейнеризация**: Docker
- **Тестирование**: JUnit 5, Mockito, Testcontainers

## **Микросервисы**

### **1. Resume Analyzer**
- Парсинг загруженного резюме.
- Анализ ключевых навыков и выявление сильных и слабых сторон.

### **2. Resume Generator**
- Генерация профессиональных резюме на основе входных данных.
- Использование ИИ для улучшения текстов и шаблонов.

### **3. API Gateway**
- Центральная точка маршрутизации запросов.
- Управление безопасностью и авторизацией.

### **4. Common Module**
- Общие зависимости и утилиты, используемые между микросервисами.

## **Как запустить проект локально**

### **Требования**
- Java 21
- Gradle
- Docker (для баз данных)
- PostgreSQL (локальный или в контейнере)
- MongoDB (локальный или в контейнере)

### **Шаги запуска**

1. **Клонируйте репозиторий:**
   ```bash
   git clone https://github.com/your-repo/resume-analyzer-backend.git
   cd resume-analyzer-backend
   ```

2. **Запустите базы данных через Docker:**
   ```bash
   docker-compose up -d
   ```

3. **Соберите проект:**
   ```bash
   ./gradlew clean build
   ```

4. **Запустите каждый микросервис:**
   ```bash
   ./gradlew :resume-analyzer:bootRun
   ./gradlew :resume-generator:bootRun
   ./gradlew :api-gateway:bootRun
   ```

5. **Документация API:**
    - После запуска API Gateway документация будет доступна по адресу: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

6. **Запуск swagger-ui:**
   - Для запуска swagger-ui, после запуска приложения перейдите по адресу: [http://localhost:8080/swagger-ui/index.html]
7. 
## **Структура проекта**
```plaintext
resume-analyzer-backend/
├── resume-analyzer/       # Микросервис анализа резюме
├── resume-generator/      # Микросервис генерации резюме
├── api-gateway/           # API Gateway
├── common/                # Общие модули (модели, утилиты)
└── README.md              # Описание проекта
```

## **Переменные окружения**
Создайте файл `.env` в корневом каталоге для настройки:
```plaintext
POSTGRES_DB=resume_db
POSTGRES_USER=resume_user
POSTGRES_PASSWORD=resume_password
MONGO_URI=mongodb://localhost:27017/resume_db
KAFKA_BROKER=localhost:9092
``` 



### **Запуск Анализа и оценки резюме с помощью DeepSeek**
- Подключится к PostgreSql, в докере поднять zookeeper, Kafka
- Запустить Api-Gateway 
- Запустить микросервисы Resume-Analyzer и Deepseek- service
- Проверить работоспособность через Postman 
- Отправить Post запрос http://localhost:8765/api/ai/analyze , в ответе будет id
- После, подождать секунд 20 и Отправить Get запрос http://localhost:8765/api/ai/status/{id} 
- В логах можно увидеть оценку резюме от Deeepseek, в терминале можно увидеть рассуждения Deepseek


- **Kafka**: Порт 9092
- **zookeeper**: Порт 2181
- **Postgre**: Порт 5434
- **Api-Gateway**: Порт 8765

## **Тестирование**
1. **Запустите тесты:**
   ```bash
   ./gradlew test
   ```
2. **Покрытие кода:**
    - Отчёты доступны в каталоге `build/reports/tests`.

## **Grafana UI**
- Доступ к Grafana UI осуществляется после запуска docker compose по адресу http://localhost:3000/
- login:admin ,password:admin.
- Во вкладке Logs (через 20 секунд) появятся логи со всех микросервисов.
- Во вкладке Metrics - все метрики отображаются загруженные с `/actuator/prometheus`
- Во вкладке Dashboards - дашборды , находящиеся в пакете `grafana-provisioning`

## **Prometheus UI**
- Доступ к Prometheus осуществляется после запуска docker compose по адресу http://localhost:9099/
- Вкладка alerts отображает алерты , которые находятся в конфигфайле `utils/alert.rules.yml`
- Вкладка query позволяет ввести запрос для отображения метриков , например `rate(process_cpu_usage{job="resume-analyzer"}[1m])`

## **Alertmanager UI**
- Доступен по адресу http://localhost:9093/
- Alertmanager позволяет добавить настройки для алертов
- Способ оповещения можно настроить в конфигфайле `utils/alertmanager.yml`
- Для оповещения по email требуется SMTP сервер