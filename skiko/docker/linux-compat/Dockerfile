# Multi-architecture Dockerfile for building Skiko on Linux, compatible with GLIBC 2.26
# Supports both amd64 (x64) and arm64 architectures

FROM amazonlinux:2
ARG TARGETARCH

# Install basic & development tools
RUN yum install -y tar xz git wget curl zip unzip gzip && \
    yum install -y gcc10 gcc10-c++ gcc10-binutils make && \
    ln -sf /usr/bin/gcc10-gcc /usr/bin/gcc && \
    ln -sf /usr/bin/gcc10-g++ /usr/bin/g++ && \
    ln -sf /usr/bin/gcc10-ar /usr/bin/ar && \
    yum clean all

# Install Xvfb (and GLX module) for headless UI testing
RUN yum install -y xorg-x11-server-Xvfb xorg-x11-server-Xorg && \
    yum clean all

# Install libraries for native builds
RUN yum install -y \
    fontconfig fontconfig-devel \
    freetype freetype-devel \
    mesa-libGL mesa-libGL-devel \
    mesa-libEGL mesa-libEGL-devel \
    mesa-libGLU mesa-libGLU-devel \
    mesa-libgbm mesa-libgbm-devel \
    mesa-dri-drivers \
    libdrm libdrm-devel \
    libX11 libX11-devel \
    libXrandr libXrandr-devel \
    libXext libXext-devel \
    libXrender libXrender-devel \
    libXfixes libXfixes-devel \
    libXdamage libXdamage-devel \
    libXtst libXtst-devel \
    libglvnd libglvnd-devel \
    libglvnd-glx libglvnd-egl \
    dbus-devel dbus-libs && \
    yum clean all

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

# Install Android SDK
ENV ANDROID_SDK_ROOT=/android/sdk
ENV CMD_TOOLS_VERSION=13114758
ENV CMD_TOOLS_ROOT=$ANDROID_SDK_ROOT/cmdline-tools/$CMD_TOOLS_VERSION
ENV SDK_MANAGER=$CMD_TOOLS_ROOT/bin/sdkmanager
RUN mkdir -p $CMD_TOOLS_ROOT && \
    export CMD_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-${CMD_TOOLS_VERSION}_latest.zip" && \
    wget $CMD_TOOLS_URL -O /tmp/cmd-tools.zip && \
    unzip /tmp/cmd-tools.zip -d /tmp/cmd-tools && \
    rm /tmp/cmd-tools.zip && \
    mv /tmp/cmd-tools/cmdline-tools/* $CMD_TOOLS_ROOT/ && \
    rm -rf /tmp/cmd-tools
ENV ANDROID_PLATFORM=android-33
RUN yes | $SDK_MANAGER --licenses && \
    $SDK_MANAGER "platforms;$ANDROID_PLATFORM" && \
    cd $ANDROID_SDK_ROOT/platforms/$ANDROID_PLATFORM && \
    ls -1 | grep -v android.jar | xargs rm -rf
ENV NDK_VERSION=29.0.14206865
RUN $SDK_MANAGER "ndk;$NDK_VERSION" && \
    cd $ANDROID_SDK_ROOT/ndk/$NDK_VERSION && \
    ls -1 | grep -v toolchains | xargs rm -rf

# Install chromium tools
ENV DEPOT_TOOLS=/usr/depot_tools
ENV PATH=$DEPOT_TOOLS:$PATH
RUN git clone 'https://chromium.googlesource.com/chromium/tools/depot_tools.git' $DEPOT_TOOLS

# Install cross-compilation toolchain for ARM64
# This cross-compilation setup is required because Kotlin/Native doesn't support
# Linux ARM64 as a HOST platform (only as a target). When cross-compiling from x64 to ARM64,
# Kotlin/Native's linker needs ARM64 versions of system libraries.
# See: https://youtrack.jetbrains.com/issue/KT-36871
ENV ARM_TOOLCHAIN_VERSION=10.3-2021.07
ENV ARM_TOOLCHAIN_NAME=gcc-arm-${ARM_TOOLCHAIN_VERSION}-x86_64-aarch64-none-linux-gnu
ENV ARM_TOOLCHAIN_PATH=/opt/arm-gnu-toolchain
ENV PATH="/usr/local/bin:$ARM_TOOLCHAIN_PATH/bin:$PATH"
ENV ARM_TOOLCHAIN_SYSROOT=$ARM_TOOLCHAIN_PATH/aarch64-none-linux-gnu/libc
RUN if [ "$TARGETARCH" = "amd64" ]; then \
    cd /tmp && mkdir -p $ARM_TOOLCHAIN_PATH && \
    wget -q https://developer.arm.com/-/media/Files/downloads/gnu-a/$ARM_TOOLCHAIN_VERSION/binrel/$ARM_TOOLCHAIN_NAME.tar.xz && \
    tar -xf $ARM_TOOLCHAIN_NAME.tar.xz --strip-components=1 -C $ARM_TOOLCHAIN_PATH && rm $ARM_TOOLCHAIN_NAME.tar.xz && \
    ln -s $ARM_TOOLCHAIN_PATH/bin/aarch64-none-linux-gnu-g++ /usr/local/bin/aarch64-linux-gnu-g++ && \
    ln -s $ARM_TOOLCHAIN_PATH/bin/aarch64-none-linux-gnu-gcc /usr/local/bin/aarch64-linux-gnu-gcc && \
    ln -s $ARM_TOOLCHAIN_PATH/bin/aarch64-none-linux-gnu-ar /usr/local/bin/aarch64-linux-gnu-ar; \
    fi

# Copy development headers to ARM64 sysroot
ENV ARM_SYSROOT_HEADERS="fontconfig freetype2 GL X11 KHR"
RUN if [ "$TARGETARCH" = "amd64" ]; then \
    mkdir -p $ARM_TOOLCHAIN_SYSROOT/usr/include && \
    for dir in $ARM_SYSROOT_HEADERS; do \
        cp -r /usr/include/$dir $ARM_TOOLCHAIN_SYSROOT/usr/include/ || exit 1; \
    done; \
    fi

ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF"
