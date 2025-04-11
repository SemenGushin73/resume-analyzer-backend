API-Gateway

Запускается на порту 8765

1. Убедиться, что библиотека analytics-message опубликована в локальный maven репозиторий.
Если нет - зайти в панель Gradle, 
выбрать analytics-message -> Tasks -> publishing -> publishToMavenLocal

2. Убедиться, что библиотека resume-analyser-common опубликована в локальный maven репозиторий.
   - Если нет - склонируйте репозиторий:
     
     `git clone https://gitpacket.kata.academy/scm/fit/resume-analyzer-common.git`
   - Выполните:
   
     `./gradlew clean build publishToMavenLocal`

3. Запустить resume-analyzer-backend (обязательно zookeeper, kafka, postgres, postgres-analytic,
mongo, rezume-analyzer, rezume-generator, analytic-service)

4. Запустить ApiGatewayApplication