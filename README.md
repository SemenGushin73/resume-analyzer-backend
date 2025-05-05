# Resume Analyzer Backend

## 📝 Описание проекта

Resume Analyzer Backend — это микросервисное backend-приложение для анализа и генерации резюме, включающее
AI-интеграцию (DeepSeek), API Gateway и мониторинг инфраструктуры.

## 🧪 Технологии

- **Java 21 + Gradle (многомодульный проект)**
- **Spring Boot** (REST, Mongo, Kafka, Validation, Actuator)
- **Spring Cloud Gateway** (WebFlux, CircuitBreaker, LoadBalancer)
- **PostgreSQL** для анализа резюме
- **MongoDB** для шаблонов
- **Apache Kafka + Zookeeper**
- **Prometheus + Grafana + Loki + Alertmanager** для мониторинга
- **OpenTelemetry-Collector + Jaeger** для трейсов
- **Docker + Docker Compose**
- **JUnit5 / Mockito / Testcontainers**

## 🖨 Модули

- `resume-analyzer` — анализ резюме, сохранение и Kafka-интеграция
- `resume-generator` — генерация текста по шаблонам и через AI
- `deepseek-service` — вызов AI-модели через OpenRouter API
- `api-gateway` — маршрутизация, OpenAPI Proxy, CircuitBreaker
- `lib` — общий модуль с DTO, исключениями и утилитами
- `bitbucket-config-server` — централизованная конфигурация микросервисов для удобства поддержки и масштабирования

## 🚀 Запуск проекта

### 🛠️ Требования:

- Java 21
- Docker / Docker Compose

### 🔄 Клонирование и сборка:

```bash
git clone https://gitpacket.kata.academy/scm/fit/resume-analyzer-backend.git
cd resume-analyzer-backend
./gradlew clean build -x test
```

### 🚧 Запуск инфраструктуры:

```bash
docker compose -f docker-compose.yml up -d
```

### 📚 Документация Swagger:

- [http://localhost:8765/swagger-ui/index.html](http://localhost:8765/analyzer/swagger-ui/index.html)

### 🔧 Примеры запросов:

#### 1. Документация Gateway API
```http
POST /api/analyze
```
Отправляет POST запрос в GatewayController(), в теле запроса
JSON (DTO AnalysisRequest). Контроллер генерирует requestId,
вызывает KafkaConsumerService API Gateway и отправляет сообщение 
в Kafka топик RESUME_ANALYZE_REQUESTS. На этот топик подписан
слушатель AnalysisRequest(), в сервисе Resume Analyzer. Слушатель 
отправляет прочитанное сообщение в Kafka топик 
DEEPSEEK_ANALYSIS_REQUESTS, на который подписан слушатель 
processRequest() в сервисе Deepseek Service. Он в свою очередь
вызывает метод analyzeResume(), объявленный в DeepSeekService, который
делает запрос непосредственно в ИИ модель, для анализа соответствия 
текста резюме, описанию вакансии. Полученный ответ от ИИ модели,
отправляется в Kafka топик RESUME_ANALYZE_RESULTS.
Итоговое сообщение из топика RESUME_ANALYZE_RESULTS, можно вычитать
консольным Kafka Consumer.

#### 2. Документация Resume Analyzer API
```http
POST /resumes/files/upload
```
Отправляет POST запрос на сохранение файла резюме в БД. Для корректной
работы запроса необходимо, что бы в БД (postgres test_db resume_owners)
присутствовала запись с ownerId отправляемого в запросе (например
550e8400-e29b-41d4-a716-446655440000), так же необходимо создать любой
файл *.docx или *.pdf для выбора его в поле Request body file.
## 🗂️ Структура проекта

```
resume-analyzer-backend/
├── api-gateway/                # API Gateway с прокси и фолбэком
├── bitbucket-config-server/    # Централизованная конфигурация микросервисов Spring Cloud Config
├── resume-analyzer/            # Анализ резюме (PostgreSQL)
├── resume-generator/           # Генератор резюме (MongoDB)
├── deepseek-service/           # Интеграция с AI (OpenRouter)
├── lib/                        # Общие DTO, утилиты, исключения
├── docker-compose.yml          # Полный стек инфраструктуры
└── utils/                      # Настройки Prometheus, Loki, Grafana, otel-Collector
```

## 🛠️ Конфигурация Kafka и БД (Docker Compose)

- PostgreSQL: `localhost:5434`
- MongoDB: `localhost:27017`
- Kafka: `localhost:9092` внешний порт '29092'
- Redis: `localhost:6379`
- DeepSeek: `localhost:9090`
- Resume Analyzer: `localhost:8083`
- Resume Generator: `localhost:8082`
- API Gateway: `localhost:8765`
- Config Server: `localhost:8888`

## 🔍 Мониторинг

### Grafana

- [http://localhost:3000](http://localhost:3000)
- Логин: `admin`, Пароль: `admin`

### Prometheus

- [http://localhost:9099](http://localhost:9099)

### Alertmanager

- [http://localhost:9093](http://localhost:9093)

### Jaeger

- [http://localhost:16686](http://localhost:16686)

## ✅ Тестирование

```bash
./gradlew test
```

- Репорты: `build/reports/tests`

---
