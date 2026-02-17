#!/usr/bin/env bash
# Build Skia locally and publish to Maven Local
#
# This script handles git operations and delegates configuration to Gradle.
# For more control, use Gradle tasks directly:
#   ./gradlew prepareLocalSkiaBuild -Pskia.pack.dir=/path/to/skia-pack
#   ./gradlew publishToMavenLocal -Pskia.dir=/path/to/skia
#
# Environment variables:
#   SKIA_VERSION   - Skia version to build (default: from gradle.properties)
#   SKIA_TARGET    - Target platform: ios, iosSim, macos, windows, linux, wasm (default: current OS)
#   SKIA_PACK_DIR  - Skia-pack repository directory with tools/skia_release/ (default: ./skia-pack)
#   SKIA_DIR       - Skia source directory for publishing (default: $SKIA_PACK_DIR/skia)

set -e  # Exit on error
cd "$(dirname "$0")"
SCRIPT_DIR="$(pwd)"

# Use provided skia-pack directory or default
SKIA_PACK_DIR="${SKIA_PACK_DIR:-$SCRIPT_DIR/skia-pack}"

# Clone if needed
if [ ! -d "$SKIA_PACK_DIR" ]; then
    echo "Cloning skia-pack repository to $SKIA_PACK_DIR..."
    git clone https://github.com/JetBrains/skia-pack.git "$SKIA_PACK_DIR"
fi

# Convert to absolute path if relative
if [[ "$SKIA_PACK_DIR" != /* ]]; then
    SKIA_PACK_DIR="$(cd "$SKIA_PACK_DIR" && pwd)"
fi

echo "Using skia-pack directory: $SKIA_PACK_DIR"
cd "$SKIA_PACK_DIR"

# Get version from Gradle (respects SKIA_VERSION env var)
SKIA_VERSION=$(cd "$SCRIPT_DIR" && ./gradlew -q printSkiaVersion)
echo "Using Skia version: $SKIA_VERSION"

# Checkout the Skia sources corresponding to the selected version
echo "Checking out Skia sources for version: $SKIA_VERSION"
python3 script/checkout.py --version "$SKIA_VERSION"

# Build Skia binaries
cd "$SCRIPT_DIR"
echo "Building Skia binaries with Gradle..."
./gradlew prepareLocalSkiaBuild -Pskia.pack.dir="$SKIA_PACK_DIR"

# Publish Skiko to Maven Local with the built Skia binaries
# If SKIA_DIR not explicitly set, use the default location where Python scripts output built Skia
if [ -z "$SKIA_DIR" ]; then
    SKIA_DIR="$SKIA_PACK_DIR/skia"
fi

echo "Publishing Skiko to Maven Local..."
echo "Using Skia source directory: $SKIA_DIR"
./gradlew publishToMavenLocal -Pskia.dir="$SKIA_DIR"

echo "Successfully published Skia build to Maven Local"
