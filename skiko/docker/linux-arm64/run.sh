CD=$(dirname "$0")
IMAGE_NAME=$(sh $CD/image-name.sh)
SKIKO_ROOT_LOCAL=$(cd $CD/../.. && pwd)
docker run \
  --cpus 1 \
  --memory 4g \
  --tty --interactive \
  --volume $SKIKO_ROOT_LOCAL:/skiko \
  --workdir /skiko \
  $IMAGE_NAME \
  bash