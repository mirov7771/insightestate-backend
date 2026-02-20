IMAGE_NAME=$1
IMAGE_ARCHIVE_NAME=$2

docker build --platform=linux/amd64 -t "$IMAGE_NAME" -f ../Dockerfile ../
rm "$IMAGE_ARCHIVE_NAME"
docker save "$IMAGE_NAME" | gzip > "$IMAGE_ARCHIVE_NAME"
