# Multi-architecture Dockerfile for building Skiko on Linux
# Supports both amd64 (x64) and arm64 architectures
# Usage:
#   docker buildx build --platform linux/amd64,linux/arm64 -t skiko-builder:latest .
#   docker buildx build --platform linux/amd64 -t skiko-builder:latest .
#   docker buildx build --platform linux/arm64 -t skiko-builder:latest .

FROM amazonlinux:2.0.20250929.2

# Docker automatically provides TARGETARCH (amd64, arm64, etc.)
ARG TARGETARCH

# Update and install basic tools
RUN yum update -y && \
    yum install -y gcc gcc-c++ binutils make clang tar xz git python wget curl zip gzip

# Install libs & development tools
RUN yum install -y fontconfig fontconfig-devel mesa-libGLU-devel libXrandr-devel dbus-devel

# Install depot_tools for Skia
ENV DEPOT_TOOLS=/usr/depot_tools
ENV PATH=$DEPOT_TOOLS:$PATH
RUN git clone 'https://chromium.googlesource.com/chromium/tools/depot_tools.git' $DEPOT_TOOLS

# ====

# Install cross-compilation toolchain for ARM64
# Download and install aarch64 cross-compiler toolchain from ARM
RUN if [ "$TARGETARCH" = "amd64" ]; then \
    cd /tmp && \
    wget -q https://developer.arm.com/-/media/Files/downloads/gnu/13.2.rel1/binrel/arm-gnu-toolchain-13.2.rel1-x86_64-aarch64-none-linux-gnu.tar.xz && \
    tar -xf arm-gnu-toolchain-13.2.rel1-x86_64-aarch64-none-linux-gnu.tar.xz -C /opt && \
    rm arm-gnu-toolchain-13.2.rel1-x86_64-aarch64-none-linux-gnu.tar.xz && \
    ln -s /opt/arm-gnu-toolchain-13.2.Rel1-x86_64-aarch64-none-linux-gnu/bin/aarch64-none-linux-gnu-g++ /usr/local/bin/aarch64-linux-gnu-g++ && \
    ln -s /opt/arm-gnu-toolchain-13.2.Rel1-x86_64-aarch64-none-linux-gnu/bin/aarch64-none-linux-gnu-gcc /usr/local/bin/aarch64-linux-gnu-gcc && \
    ln -s /opt/arm-gnu-toolchain-13.2.Rel1-x86_64-aarch64-none-linux-gnu/bin/aarch64-none-linux-gnu-ar /usr/local/bin/aarch64-linux-gnu-ar; \
    fi

# Add ARM toolchain to PATH (put /usr/local/bin first so symlinks take precedence)
ENV PATH="/usr/local/bin:/opt/arm-gnu-toolchain-13.2.Rel1-x86_64-aarch64-none-linux-gnu/bin:${PATH}"

# Copy x86_64 development headers to ARM64 sysroot
# Headers are architecture-independent, so we can reuse x64 headers for ARM64 cross-compilation
# Copy fontconfig, freetype, GL, X11, and KHR headers from x86_64 system to ARM sysroot
RUN if [ "$TARGETARCH" = "amd64" ]; then \
    SYSROOT=/opt/arm-gnu-toolchain-13.2.Rel1-x86_64-aarch64-none-linux-gnu/aarch64-none-linux-gnu/libc && \
    mkdir -p $SYSROOT/usr/include && \
    cp -r /usr/include/fontconfig $SYSROOT/usr/include/ && \
    cp -r /usr/include/freetype2 $SYSROOT/usr/include/ && \
    cp -r /usr/include/GL $SYSROOT/usr/include/ && \
    cp -r /usr/include/X11 $SYSROOT/usr/include/ && \
    cp -r /usr/include/KHR $SYSROOT/usr/include/;  \
    fi

#######

# Install Java (architecture-specific)
ENV JAVA_HOME=/usr/java/21
ENV PATH=$JAVA_HOME/bin:$PATH
RUN if [ "$TARGETARCH" = "arm64" ]; then \
        JAVA_URL=https://corretto.aws/downloads/latest/amazon-corretto-21-aarch64-linux-jdk.tar.gz; \
        JAVA_PATTERN="amazon-corretto-21*linux-aarch64"; \
    else \
        JAVA_URL=https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.tar.gz; \
        JAVA_PATTERN="amazon-corretto-21*linux-x64"; \
    fi && \
    JAVA_ARCHIVE=/tmp/jdk.tar.gz && \
    JAVA_BASE=/usr/java/ && \
    wget $JAVA_URL --output-document $JAVA_ARCHIVE && \
    mkdir -p $JAVA_BASE && \
    tar -xzf $JAVA_ARCHIVE --directory $JAVA_BASE && \
    find $JAVA_BASE -type d -maxdepth 1 -name "$JAVA_PATTERN" -exec mv {} $JAVA_HOME \; && \
    rm $JAVA_ARCHIVE

ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF"