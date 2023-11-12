#!/usr/bin/env bash
# Variables
export VERSION=m116-51072f3-1
export DEBUG_MODE=false
export TARGET="iosSim"
export SKIKO_TARGET_FLAGS="-Pskiko.native.ios.simulatorArm64.enabled=true -Pskiko.awt.enabled=false"

# Values
if [[ $DEBUG_MODE == "true" ]]; then
  skikoBuildType=Debug
else
  skikoBuildType=Release
fi

if [[ $(uname -m) == 'arm64' ]]; then
  skikoMachine="arm64"
else
  skikoMachine="x86_64"
fi

set -e # fail fast
set -x # print all commands
cd "$(dirname "$0")"

git clone https://github.com/JetBrains/skia-pack.git || echo "skia-pack exists. You can remove it or update by hands with git pull"
cd skia-pack
python3 script/checkout.py --version "$VERSION" --reset=False
python3 script/build.py --target "$TARGET" --machine "$skikoMachine" --build-type $skikoBuildType
python3 script/archive.py --version "$VERSION" --target "$TARGET" --machine "$skikoMachine" --build-type $skikoBuildType
cd "$(dirname "$0")"

rm -rf build/classes/kotlin/* # We need to drop old cache. We can do it with ./gradlew clean as well, but it tooks longer time to redownload dependencies dir.

./gradlew publishToMavenLocal $SKIKO_TARGET_FLAGS -Pskia.dir="$(pwd)/skia-pack/skia" -Pskiko.debug=$DEBUG_MODE
