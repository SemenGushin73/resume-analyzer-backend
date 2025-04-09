# Resume Analyzer Backend

## **Описание проекта**
Resume Analyzer Backend — это набор микросервисов, реализующих анализ, генерацию и маршрутизацию резюме, включая интеграцию с AI (DeepSeek) и средствами мониторинга.

## **Технологический стек**
- **Язык**: Java 21
- **Сборщик**: Gradle
- **Фреймворки**:
    - Spring Boot (REST API, JPA, Validation, WebFlux, Actuator)
    - Spring Cloud (Gateway, LoadBalancer, Resilience4j)
- **Базы данных**:
    - PostgreSQL (основная база данных)
    - MongoDB (хранение шаблонов резюме)
- **Очереди сообщений**: Apache Kafka + Zookeeper
- **Документация API**: Springdoc OpenAPI
- **Контейнеризация и мониторинг**:
    - Docker Compose (Prometheus, Grafana, Loki, Alertmanager, Kibana)
- **Тестирование**: JUnit 5, Mockito, Testcontainers

## **Микросервисы**

### **1. Resume Analyzer**
- Парсинг и анализ резюме.
- Оценка ключевых навыков и выявление слабых мест.

### **2. Resume Generator**
- Генерация готовых резюме по шаблонам.
- Интеграция с AI для улучшения текста.

### **3. API Gateway**
- Центральная маршрутизация запросов.
- Интеграция с Swagger через прокси.

### **4. Common Module**
- Общие модели, утилиты и зависимости.

### **5. DeepSeek Integration Service**
- Генерация AI-оценки резюме.

## **Как запустить проект локально**

### **Требования**
- Java 21
- Gradle
- Docker & Docker Compose

### **Подготовка Resume-analyzer-common**
```bash
git clone https://gitpacket.kata.academy/scm/fit/resume-analyzer-common.git
cd resume-analyzer-common
./gradlew clean build publishToMavenLocal
```

### **Запуск проекта**
```bash
git clone https://github.com/your-repo/resume-analyzer-backend.git
cd resume-analyzer-backend
```

1. **Запуск зависимостей(требовательно по RAM):**
```bash
docker-compose up -d
```

лайт версия
```bash
docker compose -f docker-compose-dev.yml up -d
```

2. **Сборка проекта:**
```bash
./gradlew clean build
```

3. **Запуск всех микросервисов параллельно:**
```bash
./gradlew runAll
```

4. **Swagger UI:**
- [http://localhost:8765/analyzer/swagger-ui/index.html](http://localhost:8765/analyzer/swagger-ui/index.html)

5. **Проверка API через Postman:**
- POST `http://localhost:8765/api/ai/analyze`
- GET `http://localhost:8765/api/ai/status/{id}`

## **Структура проекта**
```plaintext
resume-analyzer-backend/
├── resume-analyzer/       # Анализ резюме
├── resume-generator/      # Генерация резюме
├── api-gateway/           # Gateway + Swagger Proxy
├── common/                # Общие модули
├── docker-compose.yml     # Инфраструктура
└── README.md
```

## **Переменные окружения**
Создайте файл `.env`:
```env
POSTGRES_DB=resume_db
POSTGRES_USER=resume_user
POSTGRES_PASSWORD=resume_password
MONGO_URI=mongodb://localhost:27017/resume_db
KAFKA_BROKER=localhost:9092
```

## **Порты сервисов**
- Kafka: `9092`
- Zookeeper: `2181`
- PostgreSQL: `5434`
- API Gateway: `8765`
- Grafana: `3000`
- Prometheus: `9099`
- Alertmanager: `9093`

## **Мониторинг и Логирование**

### **Grafana**
- [http://localhost:3000/](http://localhost:3000/)
- Login: `admin`, Password: `admin`
- Вкладка **Logs** — логи микросервисов
- Вкладка **Metrics** — `/actuator/prometheus`

### **Prometheus**
- [http://localhost:9099/](http://localhost:9099/)
- Вкладка Alerts — настраиваются через `utils/alert.rules.yml`
- Пример запроса: `rate(process_cpu_usage{job="resume-analyzer"}[1m])`

### **Alertmanager**
- [http://localhost:9093/](http://localhost:9093/)
- Конфигурация: `utils/alertmanager.yml`
- Email-оповещения требуют SMTP сервер

## **Тестирование**
```bash
./gradlew test
```
- Результаты: `build/reports/tests`

---