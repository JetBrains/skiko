#!/usr/bin/env bash
###### Variables you can edit to change build config ######
VERSION="m116-51072f3-1" # Version of Skia m###-commit-sha-#. This commit sha will be cloned from repository https://github.com/JetBrains/skia
DEBUG_MODE="false"
TARGET="wasm" # possible values: "ios", "iosSim", "macos", "windows", "linux", "wasm", "android", "tvos", "tvosSim"
SKIKO_TARGET_FLAGS="-Pskiko.native.ios.simulatorArm64.enabled=true -Pskiko.awt.enabled=false"
###########################################################

if [[ $DEBUG_MODE == "true" ]]; then
  skikoBuildType=Debug
else
  skikoBuildType=Release
fi

case $TARGET in
  "ios")
    SKIKO_TARGET_FLAGS="-Pskiko.native.ios.arm64.enabled=true -Pskiko.awt.enabled=false"
    skikoMachines=("arm64")
    ;;
  "iosSim")
    if [[ $(uname -m) == 'arm64' ]]; then
      SKIKO_TARGET_FLAGS="-Pskiko.native.ios.simulatorArm64.enabled=true -Pskiko.awt.enabled=false"
      skikoMachines=("arm64")
    else
      SKIKO_TARGET_FLAGS="-Pskiko.native.ios.x64.enabled=true -Pskiko.awt.enabled=false"
      skikoMachines=("x64")
    fi
    ;;
  "macos")
    SKIKO_TARGET_FLAGS="-Pskiko.awt.enabled=true"
    if [[ $(uname -m) == 'arm64' ]]; then
      skikoMachines=("arm64", "x64")
    else
      skikoMachines=("x64")
    fi
    ;;
  "windows")
    SKIKO_TARGET_FLAGS="-Pskiko.awt.enabled=true"
    if [[ $(uname -m) == 'arm64' ]]; then
      skikoMachines=("arm64")
    else
      skikoMachines=("x64")
    fi
    ;;
  "linux")
    SKIKO_TARGET_FLAGS="-Pskiko.awt.enabled=true"
    if [[ $(uname -m) == 'arm64' ]]; then
      skikoMachines=("arm64")
    else
      skikoMachines=("x64")
    fi
    ;;
  "wasm")
    SKIKO_TARGET_FLAGS="-Pskiko.wasm.enabled=true -Pskiko.awt.enabled=false"
    if [[ $(uname -m) == 'arm64' ]]; then
      skikoMachines=("arm64")
    else
      skikoMachines=("x64")
    fi
    ;;
  *)
    echo "can't determine skia target"; exit 1
    ;;
esac

set -e # fail fast
set -x # print all commands
cd "$(dirname "$0")"

git clone https://github.com/JetBrains/skia-pack.git || echo "skia-pack exists. You can remove it or update by hands with git pull"
cd skia-pack
([ ! -d "skia" ] && python3 script/checkout.py --version "$VERSION") || echo "skip checkout, because directory skia-pack/skia already exists"
for skikoMachine in ${skikoMachines[@]}; do
  python3 script/build.py --target "$TARGET" --machine "$skikoMachine" --build-type $skikoBuildType
  python3 script/archive.py --version "$VERSION" --target "$TARGET" --machine "$skikoMachine" --build-type $skikoBuildType
done
cd "$(dirname "$0")"

rm -rf build/classes/kotlin/* # We need to drop old cache. We can do it with ./gradlew clean as well, but it tooks longer time to redownload dependencies dir.

./gradlew publishToMavenLocal $SKIKO_TARGET_FLAGS -Pskia.dir="$(pwd)/skia-pack/skia" -Pskiko.debug=$DEBUG_MODE
