# Multi-architecture Dockerfile for building Skiko on Linux, compatiable with GLIBC 2.26
# Supports both amd64 (x64) and arm64 architectures

FROM amazonlinux:2
ARG TARGETARCH

# Install basic & development tools
RUN yum install -y tar xz git wget curl zip gzip && \
    yum install -y gcc10 gcc10-c++ gcc10-binutils make && \
    ln -sf /usr/bin/gcc10-gcc /usr/bin/gcc && \
    ln -sf /usr/bin/gcc10-g++ /usr/bin/g++ && \
    yum clean all

# Install libraries
RUN yum install -y fontconfig fontconfig-devel mesa-libGLU-devel libXrandr-devel dbus-devel && \
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

# Install chromium tools
ENV DEPOT_TOOLS=/usr/depot_tools
ENV PATH=$DEPOT_TOOLS:$PATH
RUN git clone 'https://chromium.googlesource.com/chromium/tools/depot_tools.git' $DEPOT_TOOLS

# Install cross-compilation toolchain for ARM64
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
RUN if [ "$TARGETARCH" = "amd64" ]; then \
    mkdir -p $ARM_TOOLCHAIN_SYSROOT/usr/include && \
    cp -r /usr/include/fontconfig $ARM_TOOLCHAIN_SYSROOT/usr/include/ && \
    cp -r /usr/include/freetype2 $ARM_TOOLCHAIN_SYSROOT/usr/include/ && \
    cp -r /usr/include/GL $ARM_TOOLCHAIN_SYSROOT/usr/include/ && \
    cp -r /usr/include/X11 $ARM_TOOLCHAIN_SYSROOT/usr/include/ && \
    cp -r /usr/include/KHR $ARM_TOOLCHAIN_SYSROOT/usr/include/;  \
    fi

ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF"
