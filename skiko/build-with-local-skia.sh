#!/usr/bin/env bash
# Build Skia locally and publish to Maven Local
#
# This script handles git operations and delegates configuration to Gradle.
# For more control, use Gradle tasks directly:
#   ./gradlew buildSkiaLocally -Pskia.target=macos -Pskia.version=m138-80d088a-2
#
# Environment variables:
#   SKIA_VERSION - Skia version to build (default: from gradle.properties)
#   SKIA_TARGET  - Target platform: ios, iosSim, macos, windows, linux, wasm (default: current OS)
#   SKIA_DIR     - Skia source directory (default: auto-detected)

set -e  # Exit on error
cd "$(dirname "$0")"
SCRIPT_DIR="$(pwd)"

# Detect or use provided Skia directory
SKIA_DIR="${SKIA_DIR:-$(./gradlew -q detectSkiaDir 2>/dev/null || echo "$SCRIPT_DIR/skia-pack")}"

# Clone if needed
if [ ! -d "$SKIA_DIR" ]; then
    echo "Cloning Skia repository to $SKIA_DIR..."
    git clone https://github.com/JetBrains/skia-pack.git "$SKIA_DIR"
fi

# Convert to absolute path if relative
if [[ "$SKIA_DIR" != /* ]]; then
    SKIA_DIR="$(cd "$SKIA_DIR" && pwd)"
fi

echo "Using Skia directory: $SKIA_DIR"
cd "$SKIA_DIR"

# Get version from Gradle (respects SKIA_VERSION env var)
SKIA_VERSION=$(cd "$SCRIPT_DIR" && ./gradlew -q printSkiaVersion)
echo "Using Skia version: $SKIA_VERSION"

# Checkout specific commit
SKIA_COMMIT=$(echo "$SKIA_VERSION" | sed -E 's/m[0-9]+-([0-9a-f]+).*/\1/')
if [ -n "$SKIA_COMMIT" ]; then
    echo "Checking out commit: $SKIA_COMMIT"
    git fetch origin 2>/dev/null || true
    git checkout "$SKIA_COMMIT" 2>/dev/null || \
        git checkout "origin/$SKIA_COMMIT" 2>/dev/null || \
        echo "Using current commit"
fi

# Build Skia and publish via Gradle task
cd "$SCRIPT_DIR"
echo "Building Skia with Gradle..."
./gradlew buildSkiaLocally -Pskia.dir="$SKIA_DIR"

echo "Successfully published Skia build to Maven Local"
