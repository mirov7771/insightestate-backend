#!/bin/sh

SERVER_HOST="77.238.232.18"
SERVER_USER="root"
SERVER_PASSWORD="Tfe28g96W64juX3P"

LOCAL_ESTATE_IMAGES_FILE_ZIP=/Users/nemodev/Downloads/estate/images/estate-images.zip # Тут меняем на свой путь файл должен называться estate-images.zip
SERVER_ESTATE_IMAGES_DIR="/home/docker/estate/images"

# Загружаем архив фото
sh server-upload-file.sh $SERVER_HOST $SERVER_USER $SERVER_PASSWORD $LOCAL_ESTATE_IMAGES_FILE_ZIP $SERVER_ESTATE_IMAGES_DIR
sshpass -p $SERVER_PASSWORD ssh $SERVER_USER@$SERVER_HOST sudo unzip $SERVER_ESTATE_IMAGES_DIR/estate-images.zip