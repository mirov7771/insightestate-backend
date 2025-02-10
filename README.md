# Платежный шлюз

## Запуск
- 1 Запустить SpringBoot Application.kt в режиме debug, в настройках конфигурации запуска idea указать профиль dev
- 2 При запуске сервиса автоматически запускается окружение из dev/docker/docker-compose.yml
- 2.1 Иногда могут быть проблемы при запуске зависимых между собой контейнеров
  В таком случае запускаем их в ручную из docker-compose файла, в файле application-dev.yml отключаем запуск docker-compose
- 2.2 После запуска docker-compose.yml подключиться к бд fo-mssql и применить скрипты dev/docker/database/fo/*

## Доступное API
- 1 [Swagger Open Api UI](http://localhost:8080/swagger-ui.html)
