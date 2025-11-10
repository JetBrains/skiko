#!/bin/bash
# Build multi-architecture Docker image for Skiko
#
# Builds a true multi-arch image (amd64 + arm64) and pushes to a registry.
# This creates a single image tag that works on both platforms.
#
# Usage:
#   ./build_image.sh FULL_IMAGE_NAME
#
# Parameters:
#   FULL_IMAGE_NAME - Registry URL + image tag (default: localhost:5000)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FULL_IMAGE_NAME="$1"

echo "========================================="
echo "Building Multi-Arch Docker Image"
echo "========================================="
echo "Image: $FULL_IMAGE_NAME"
echo "Platforms: linux/amd64, linux/arm64"
echo ""

# Build multi-arch image
docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t "$FULL_IMAGE_NAME" \
    --push \
    "$SCRIPT_DIR"

echo ""
echo "========================================="
echo "✓ Multi-Arch Image Built Successfully"
echo "========================================="
echo "Image: $FULL_IMAGE_NAME"