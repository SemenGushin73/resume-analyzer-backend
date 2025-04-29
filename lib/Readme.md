# Resume Analyzer Common

***

## **Описание проекта**

Resume Analyzer Common - библиотека содержащая общие компоненты проекта Resume Analyzer

### **Содержит**

- DTO классы (resume и все зависимые подклассы)
- Exception
    - Общие кастомные исключения для всех модулей
    - Aspect - перехватчик исключений
    - Annotations - аннотации для использования над методами
- Kafka
    - Config (топики и общие настройки kafka)
    - Dto реквесты для передачи сообщений в kafka
- resources
    - common-actuator.yml - конфиг actuator
    - common-kafka.yaml - общие переменные kafka

***

### **Аннотации**

**Разместите аннотацию над методом который может выкинуть исключение**

**Aspect обернет исключение ориентируясь на аннотацию**

**Try-catch можно не использовать, Spring-AOP предполагает оборачивание всех исключений из метода автономно**

- Пример использования - deepseek-service:webapp.deepseek.application.service метод: analyzeResumeScore

**Доступные аннотации**

- @ThrowsAIProcessingException
- @ThrowsExternalServiceException
- @ThrowsInvalidRequestException
- @ThrowsKafkaProcessingException
- @ThrowsResourceNotFoundException
