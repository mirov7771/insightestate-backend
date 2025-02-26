#!/bin/sh

SERVER_HOST="77.238.232.18"
SERVER_USER="root"
SERVER_PASSWORD="Tfe28g96W64juX3P"

IMAGE_ARCHIVE_NAME=estate-images.zip
LOCAL_ESTATE_IMAGES_FILE_ZIP=/Users/nemodev/Downloads/estate/images/$IMAGE_ARCHIVE_NAME # Тут меняем на свой путь файл должен называться estate-images.zip
SERVER_ESTATE_IMAGES_DIR="/home/docker/estate/images"

# Загружаем архив фото
sh server-upload-file.sh $SERVER_HOST $SERVER_USER $SERVER_PASSWORD $LOCAL_ESTATE_IMAGES_FILE_ZIP $SERVER_ESTATE_IMAGES_DIR
# Распаковываем архив
sshpass -p $SERVER_PASSWORD ssh $SERVER_USER@$SERVER_HOST "cd $SERVER_ESTATE_IMAGES_DIR && unzip -j -o $IMAGE_ARCHIVE_NAME"
# Удаляем архив
sshpass -p $SERVER_PASSWORD ssh $SERVER_USER@$SERVER_HOST "cd $SERVER_ESTATE_IMAGES_DIR && rm $IMAGE_ARCHIVE_NAME"