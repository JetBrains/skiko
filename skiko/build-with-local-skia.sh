#!/usr/bin/env bash
####### Variables you can edit to change build config, or set same environment variables before script execution #######
SKIA_VERSION="${SKIA_VERSION:="m116-51072f3-1"}" # Version of Skia m###-commit-sha-#. This commit sha will be cloned from repository https://github.com/JetBrains/skia
SKIA_DEBUG_MODE="${SKIA_DEBUG_MODE:="false"}" # in debug mode Skiko will be published with postix "+debug", for example "0.0.0-SNAPSHOT+debug"
SKIA_TARGET="${SKIA_TARGET:="iosSim"}" # possible values: "ios", "iosSim", "macos", "windows", "linux", "wasm", "android", "tvos", "tvosSim"
# For M1 Mac use "iosSim" to build for simulator, and ios to build for device.
# For Intel Mac - use "ios" target to build for iOS x64 simulator.
# For Desktop JVM use "macos", "windows", "linux"
########################################################################################################################

if [[ $SKIA_DEBUG_MODE == "true" ]]; then
  skikoBuildType=Debug
else
  skikoBuildType=Release
fi

case $SKIA_TARGET in
  "ios")
    if [[ $(uname -m) == 'arm64' ]]; then
      SKIKO_TARGET_FLAGS="-Pskiko.native.ios.arm64.enabled=true -Pskiko.awt.enabled=false"
      skikoMachines=("arm64")
    else
      SKIKO_TARGET_FLAGS="-Pskiko.native.ios.x64.enabled=true -Pskiko.awt.enabled=false"
      skikoMachines=("x64")
    fi
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
SCRIPT_DIR="$(pwd)"

git clone https://github.com/JetBrains/skia-pack.git || echo "skia-pack exists. You can remove it or update by hands with git pull"
cd skia-pack
[ -d "skia" ] && echo "skip cript/checkout.py, because directory skia-pack/skia already exists"
[ ! -d "skia" ] && python3 script/checkout.py --version "$SKIA_VERSION"
for skikoMachine in ${skikoMachines[@]}; do
  python3 script/build.py --target "$SKIA_TARGET" --machine "$skikoMachine" --build-type $skikoBuildType
  python3 script/archive.py --version "$SKIA_VERSION" --target "$SKIA_TARGET" --machine "$skikoMachine" --build-type $skikoBuildType
done
cd "$SCRIPT_DIR"

rm -rf build/classes/kotlin/* # We need to drop old cache. We can do it with ./gradlew clean as well, but it tooks longer time to redownload dependencies dir.

./gradlew publishToMavenLocal $SKIKO_TARGET_FLAGS -Pskia.dir="$(pwd)/skia-pack/skia" -Pskiko.debug=$SKIA_DEBUG_MODE
