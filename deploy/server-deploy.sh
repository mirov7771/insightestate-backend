#!/bin/sh
CONTAINER_NAME="insightestate-backend"
IMAGE_NAME="nemodev/${CONTAINER_NAME}"
IMAGE_ARCHIVE_NAME="${CONTAINER_NAME}.tar.gz"

SERVER_HOST="77.238.232.18"
SERVER_USER="root"
SERVER_PASSWORD="Tfe28g96W64juX3P"

SERVER_DOCKER_DIR="/home/docker"

SERVER_DOCKER_IMAGE_DIR="${SERVER_DOCKER_DIR}/images"
SERVER_IMAGE_PATH="${SERVER_DOCKER_IMAGE_DIR}/${IMAGE_ARCHIVE_NAME}"

SERVER_DOCKER_COMPOSE_DIR="${SERVER_DOCKER_DIR}"
DOCKER_COMPOSE_FILE="docker-compose.yml"
SERVER_DOCKER_COMPOSE_FILE="${SERVER_DOCKER_COMPOSE_DIR}/${DOCKER_COMPOSE_FILE}"

# Удаляем контейнер + образ
sh docker-remove.sh $CONTAINER_NAME $IMAGE_NAME
## Собираем артефакты
cd ../ && sh build-local.sh && cd deploy
## Собираем новый образ и экспортируем в архив
sudo sh docker-build.sh $IMAGE_NAME $IMAGE_ARCHIVE_NAME
#
## Загружаем на сервер образ
sshpass -p $SERVER_PASSWORD scp $IMAGE_ARCHIVE_NAME $SERVER_USER@$SERVER_HOST:$SERVER_DOCKER_IMAGE_DIR
# Загружаем docker-compose
sshpass -p $SERVER_PASSWORD scp $DOCKER_COMPOSE_FILE $SERVER_USER@$SERVER_HOST:$SERVER_DOCKER_DIR
# Подключаемся к серверу удаляем контейнер + образ
sshpass -p $SERVER_PASSWORD ssh $SERVER_USER@$SERVER_HOST sudo 'bash -s' < docker-remove.sh $CONTAINER_NAME $IMAGE_NAME
# Подключаемся к серверу загружаем образ
sshpass -p $SERVER_PASSWORD ssh $SERVER_USER@$SERVER_HOST sudo 'bash -s' < docker-load-image.sh $SERVER_IMAGE_PATH
# Подключаемся к серверу запускаем контейнер
sshpass -p $SERVER_PASSWORD ssh $SERVER_USER@$SERVER_HOST sudo 'bash -s' < docker-run.sh $SERVER_DOCKER_COMPOSE_FILE $CONTAINER_NAME
