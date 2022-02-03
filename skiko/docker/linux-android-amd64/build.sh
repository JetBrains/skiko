CD=$(dirname "$0")
IMAGE_NAME=$(sh $CD/image-name.sh)
docker build -t $IMAGE_NAME $CD