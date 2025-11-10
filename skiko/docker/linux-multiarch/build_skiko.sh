#!/bin/bash
# Build LinuxX64 and LinuxArm64 natively using multi-architecture Docker image
#
# This script uses Docker's native multi-platform support to build artifacts
# for the host architecture. Each architecture runs natively (no cross-compilation),
# producing complete builds including JVM bindings.
#
# Usage:
#   ./build_skiko.sh [ARCH]
#
# Parameters:
#   ARCH - Target architecture: x64, arm64, or both (default: both)
#
# Examples:
#   ./build_skiko.sh        # Build both x64 and arm64 natively
#   ./build_skiko.sh x64    # Build only x64
#   ./build_skiko.sh arm64  # Build only arm64
#
# Note: Building "both" requires Docker buildx with multi-platform support
#       or separate runs on x64 and arm64 machines.

set -e  # Exit on error

ARCH="${1:-both}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
GRADLE_CACHE="${GRADLE_CACHE:-$REPO_ROOT/.gradle-cache}"

FULL_IMAGE="registry.jetbrains.team/p/tbx/build-images/skiko-builder:latest"

# Ensure Gradle cache directory exists
mkdir -p "$GRADLE_CACHE"

echo "========================================="
echo "Building Skiko for Linux"
echo "========================================="
echo "Architecture: $ARCH"
echo "Docker image: $FULL_IMAGE"
echo "Repository: $REPO_ROOT"
echo "Gradle cache: $GRADLE_CACHE"
echo ""

build_x64() {
    echo "Building Linux x64 (native)..."
    docker run --rm \
        --platform linux/amd64 \
        --mount type=bind,source="$REPO_ROOT",target=/host/skiko \
        --mount type=bind,source="$GRADLE_CACHE",target=/root/.gradle \
        "$FULL_IMAGE" \
        bash -c "cd /host/skiko/skiko && ./gradlew -Pskiko.arch=x64 -Pskiko.native.linux.enabled=true compileKotlinLinuxX64 linuxX64SourcesJar linkJvmBindingsLinuxX64 --no-daemon --stacktrace"
    echo "✓ Linux x64 build completed"
}

build_arm64() {
    echo "Building Linux ARM64 (hybrid approach)..."
    echo ""
    echo "Step 1: Cross-compiling Kotlin/Native on x64..."
    docker run --rm \
        --platform linux/amd64 \
        --mount type=bind,source="$REPO_ROOT",target=/host/skiko \
        --mount type=bind,source="$GRADLE_CACHE",target=/root/.gradle \
        "$FULL_IMAGE" \
        bash -c "cd /host/skiko/skiko && ./gradlew -Pskiko.arch=arm64 -Pskiko.native.linux.enabled=true compileKotlinLinuxArm64 linuxArm64SourcesJar --no-daemon --stacktrace"
    echo "✓ Kotlin/Native cross-compilation completed"
    echo ""
    echo "Step 2: Building JVM bindings natively on ARM64..."
    docker run --rm \
        --platform linux/arm64 \
        --mount type=bind,source="$REPO_ROOT",target=/host/skiko \
        --mount type=bind,source="$GRADLE_CACHE",target=/root/.gradle \
        "$FULL_IMAGE" \
        bash -c "cd /host/skiko/skiko && ./gradlew -Pskiko.arch=arm64 -Pskiko.native.linux.enabled=true linkJvmBindingsLinuxArm64 --no-daemon --stacktrace"
    echo "✓ Linux ARM64 build completed"
}

case "$ARCH" in
    x64)
        build_x64
        ;;
    arm64)
        build_arm64
        ;;
    both)
        build_x64
        build_arm64
        ;;
    *)
        echo "Error: Invalid architecture '$ARCH'"
        echo "Valid options: x64, arm64, both"
        exit 1
        ;;
esac

echo ""
echo "========================================="
echo "✓ Build completed successfully"
echo "========================================="
