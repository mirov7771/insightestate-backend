# BACK-END
## https://www.insightestate.com/listing

# Local Soft
- 1 Установить https://www.cyberciti.biz/faq/how-to-install-sshpass-on-macos-os-x/ для простоты deploy

# Local run
- 1 Запустить Springboot Application.kt в режиме debug, в настройках конфигурации запуска idea указать профиль local
- 2 При запуске сервиса автоматически запускается окружение из local/docker/docker-compose.yml
- 3 Перейти по ссылке [localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) для просмотра swagger и его использования

# Deploy
- 1 Перейти в deploy
- 2 Выполнить команду sh server-deploy.sh дождаться окончания docker image загружается не быстро
- 2.1 Если не удалось поставить sshpass то удалить в скрипте sshpass -p $SERVER_PASSWORD и вводить пароль мануально каждый раз 

# Как загрузить фото объектов
- 1 Подготовить картинки в zip архиве с именем estate-images.zip
- 2 Открыть скрипт deploy/server-upload-estate-images.sh и указать путь до архива
- 3 Запустить скрипт, дождаться загрузки архива и его распаковки на сервере
- 4 Вызвать api /v1/estate/load/images и ожидать пока оно отработает загрузка занимает время т.к фото много