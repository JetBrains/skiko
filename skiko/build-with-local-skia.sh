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

# Checkout specific commit
SKIA_COMMIT=$(echo "$SKIA_VERSION" | sed -E 's/m[0-9]+-([0-9a-f]+).*/\1/')
if [ -n "$SKIA_COMMIT" ]; then
    echo "Checking out commit: $SKIA_COMMIT"

    # Fetch from origin (warn if fails but continue)
    if ! git fetch origin; then
        echo "WARNING: Failed to fetch from origin, using local commits only"
    fi

    # Try to checkout the commit
    if ! git checkout "$SKIA_COMMIT" 2>&1; then
        if ! git checkout "origin/$SKIA_COMMIT" 2>&1; then
            echo "ERROR: Could not checkout commit $SKIA_COMMIT"
            echo "Available recent commits in skia-pack repo:"
            git log --oneline -10
            exit 1
        fi
    fi

    # Confirm successful checkout
    ACTUAL_COMMIT=$(git rev-parse HEAD)
    echo "Successfully checked out: $ACTUAL_COMMIT"
fi

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
