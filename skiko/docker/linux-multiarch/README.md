# Multi-Architecture Linux Build for Skiko

This directory contains a Docker setup for building Skiko natively on both x64 (amd64) and ARM64 (aarch64) Linux platforms using a single multi-architecture Docker image.

## Overview

- **Single Docker image tag** that works on both x64 and ARM64
- **Native builds** for each architecture (no cross-compilation limitations)
- **Complete artifacts** including Kotlin/Native and JVM bindings for both platforms
- **Automatic platform selection** by Docker

## Quick Start

```bash
cd skiko/docker/linux-multiarch

# 1. Build the multi-arch Docker image
./build_image.sh

# 2. Build Skiko for x64
./build_skiko.sh x64

# 3. Build Skiko for ARM64
./build_skiko.sh arm64

# 4. Or build both
./build_skiko.sh both
```

## How It Works

### Multi-Arch Docker Image

The image is built for **both linux/amd64 and linux/arm64** and pushed to a registry (localhost:5000 by default). This creates a **manifest list** that Docker uses to automatically select the right platform:

```
localhost:5000/skiko-builder:latest
├── linux/amd64 variant
└── linux/arm64 variant
```

When you use the image, Docker automatically pulls and runs the variant matching your host architecture.

### Building the Docker Image

```bash
./build_image.sh                    # Uses localhost:5000 (local registry)
./build_image.sh myregistry.com     # Uses remote registry
```

The script will automatically start a local Docker registry if needed.

**Environment variables:**
- `DOCKER_IMAGE`: Image name (default: `skiko-builder`)
- `DOCKER_TAG`: Image tag (default: `latest`)

### Building Skiko

```bash
./build_skiko.sh x64      # Build only x64
./build_skiko.sh arm64    # Build only ARM64
./build_skiko.sh both     # Build both architectures
```

**Environment variables:**
- `DOCKER_REGISTRY`: Registry URL (default: `localhost:5000`)
- `DOCKER_IMAGE`: Image name (default: `skiko-builder`)
- `DOCKER_TAG`: Image tag (default: `latest`)

**Example with remote registry:**
```bash
DOCKER_REGISTRY=myregistry.com ./build_skiko.sh both
```

## What Gets Built

### x64 (amd64)
- `compileKotlinLinuxX64` - Kotlin/Native compilation
- `compileNativeBridgesLinuxX64` - Native C++ bridges
- `linkNativeBridgesLinuxX64` - Link native bridges
- `linkJvmBindingsLinuxX64` - JVM bindings (JNI)
- `linuxX64SourcesJar` - Sources jar

### ARM64 (aarch64)
- `compileKotlinLinuxArm64` - Kotlin/Native compilation
- `compileNativeBridgesLinuxArm64` - Native C++ bridges
- `linkNativeBridgesLinuxArm64` - Link native bridges
- `linkJvmBindingsLinuxArm64` - JVM bindings (JNI)
- `linuxArm64SourcesJar` - Sources jar

ARM64 builds entirely natively using the ARM64 variant of the Docker image.

## Architecture

### Why a Registry?

Docker's local daemon can only store **one platform per image tag**. To have a single tag that works for both platforms, we need a **manifest list**, which can only be created and stored in a registry.

The local registry (`localhost:5000`) runs as a Docker container and provides this capability without needing external infrastructure.

### Multi-Arch Manifest

When you build with `./build_image.sh`, it creates a manifest list:

```
docker buildx build --platform linux/amd64,linux/arm64 \
  -t localhost:5000/skiko-builder:latest --push .
```

This creates one manifest list pointing to two platform-specific images. When you run:

```bash
docker run localhost:5000/skiko-builder:latest
```

Docker automatically:
1. Fetches the manifest list
2. Checks your host platform
3. Pulls only the matching platform image
4. You can override with `--platform linux/amd64` or `--platform linux/arm64`

## Comparison with Cross-Compilation

| Feature | Multi-Arch (this setup) | Cross-Compilation (../linux-amd64) |
|---------|------------------------|-------------------------------------|
| JVM Bindings | ✅ Both architectures | ✅ x64 only |
| Build Speed | Moderate | Fast (x64), Fast (ARM64 K/N only) |
| System Libraries | Native for each arch | x64 only |
| Setup | Requires registry | Simpler (local only) |
| Use Case | Production, complete builds | CI/CD, quick iterations |

## Troubleshooting

### "connection refused" when accessing localhost:5000

The local registry isn't running. Run:
```bash
docker run -d -p 5000:5000 --restart=always --name registry registry:2
```

Or just run `./build_image.sh` which will start it automatically.

### "manifest unknown" error

The image hasn't been pushed to the registry yet. Run `./build_image.sh` first.

### Slow ARM64 builds on x64

This is normal when Docker uses QEMU emulation for the ARM64 JVM bindings step. The Kotlin/Native step runs on x64 and is fast.

### Want to use a remote registry?

```bash
# Build and push to remote registry
./build_image.sh docker.io/myuser

# Use it for builds
DOCKER_REGISTRY=docker.io/myuser ./build_skiko.sh both
```

Make sure you're logged in: `docker login docker.io`
