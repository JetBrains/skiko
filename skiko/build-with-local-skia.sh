#!/usr/bin/env bash
# Build Skia locally and publish to Maven Local
#
# This script handles git operations and delegates configuration to Gradle.
# For more control, use Gradle tasks directly:
#   ./gradlew prepareLocalSkiaBuild -Pskia.repo.dir=/path/to/skia
#   ./gradlew publishToMavenLocal -Pskia.dir=/path/to/skia
#
# Environment variables:
#   SKIA_VERSION   - Skia version to build (default: from gradle.properties)
#   SKIA_TARGET    - Target platform: ios, iosSim, macos, windows, linux, wasm (default: current OS)
#   SKIA_REPO_DIR  - Skia repository directory with tools/skia_release/ (default: ./skia)
#   SKIA_DIR       - Skia source directory for publishing (default: $SKIA_REPO_DIR)

set -e  # Exit on error
cd "$(dirname "$0")"
SCRIPT_DIR="$(pwd)"

# Use provided Skia repository directory or default
SKIA_REPO_DIR="${SKIA_REPO_DIR:-${SKIA_PACK_DIR:-$SCRIPT_DIR/skia}}"

# Clone if needed
if [ ! -d "$SKIA_REPO_DIR" ]; then
    echo "Cloning skia repository to $SKIA_REPO_DIR..."
    git clone https://github.com/JetBrains/skia.git "$SKIA_REPO_DIR"
fi

# Convert to absolute path if relative
if [[ "$SKIA_REPO_DIR" != /* ]]; then
    SKIA_REPO_DIR="$(cd "$SKIA_REPO_DIR" && pwd)"
fi

echo "Using skia repository: $SKIA_REPO_DIR"
cd "$SKIA_REPO_DIR"

# Get version from Gradle (respects SKIA_VERSION env var)
SKIA_VERSION=$(cd "$SCRIPT_DIR" && ./gradlew -q printSkiaVersion)
echo "Using Skia version: $SKIA_VERSION"

# Build Skia binaries
cd "$SCRIPT_DIR"
echo "Building Skia binaries with Gradle..."
./gradlew prepareLocalSkiaBuild -Pskia.repo.dir="$SKIA_REPO_DIR"

# Publish Skiko to Maven Local with the built Skia binaries
# If SKIA_DIR is not explicitly set, use the same checkout that contains the
# integrated release scripts and the built outputs.
if [ -z "$SKIA_DIR" ]; then
    SKIA_DIR="$SKIA_REPO_DIR"
fi

echo "Publishing Skiko to Maven Local..."
echo "Using Skia source directory: $SKIA_DIR"
./gradlew publishToMavenLocal -Pskia.dir="$SKIA_DIR"

echo "Successfully published Skia build to Maven Local"
