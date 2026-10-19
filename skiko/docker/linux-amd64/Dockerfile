FROM ubuntu:22.04
ARG DEBIAN_FRONTEND=noninteractive

RUN apt update -y && \
    apt install binutils build-essential software-properties-common -y && \
    apt install zip unzip git python3 curl wget xvfb -y && \
    apt install fontconfig libfontconfig1-dev libegl1-mesa-dev libglu1-mesa-dev libxrandr-dev libdbus-1-dev -y && \
    apt install libvulkan1 libvulkan-dev mesa-vulkan-drivers -y && \
    apt install openjdk-21-jdk -y && \
    apt install gcc-11 g++-11 -y && \
    update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-11 60 --slave /usr/bin/g++ g++ /usr/bin/g++-11 && \
    update-alternatives --config gcc && \
    rm -rf /var/lib/apt/lists/*

# Install Android SDK
ENV ANDROID_SDK_ROOT=/android/sdk
ENV ANDROID_HOME=$ANDROID_SDK_ROOT
ENV CMD_TOOLS_VERSION=13114758
ENV CMD_TOOLS_ROOT=$ANDROID_SDK_ROOT/cmdline-tools/$CMD_TOOLS_VERSION
ENV SDK_MANAGER=$CMD_TOOLS_ROOT/bin/sdkmanager
RUN mkdir -p $CMD_TOOLS_ROOT && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-${CMD_TOOLS_VERSION}_latest.zip -O /tmp/cmd-tools.zip && \
    unzip /tmp/cmd-tools.zip -d /tmp/cmd-tools && \
    rm /tmp/cmd-tools.zip && \
    mv /tmp/cmd-tools/cmdline-tools/* $CMD_TOOLS_ROOT/ && \
    rm -rf /tmp/cmd-tools
ENV ANDROID_PLATFORM=android-35
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

# Install ARM64 libraries for cross-compilation
RUN dpkg --add-architecture arm64 && echo \
        "deb [arch=arm64] http://ports.ubuntu.com/ubuntu-ports jammy main restricted\n" \
        "deb [arch=arm64] http://ports.ubuntu.com/ubuntu-ports jammy-updates main restricted\n" \
        "deb [arch=arm64] http://ports.ubuntu.com/ubuntu-ports jammy-security main restricted universe multiverse\n" \
        "deb [arch=arm64] http://ports.ubuntu.com/ubuntu-ports jammy-backports main restricted universe multiverse" \
    | tee /etc/apt/sources.list.d/arm-cross-compile-sources.list && \
    sed -i -E "s#(deb) (http://(archive|security).ubuntu.com/ubuntu.+)#\1 [arch=amd64] \2#" /etc/apt/sources.list && \
    apt update -y && \
    apt install libfontconfig1-dev:arm64 libegl1-mesa-dev:arm64 libglu1-mesa-dev:arm64 libxrandr-dev:arm64 libdbus-1-dev:arm64 -y && \
    rm -rf /var/lib/apt/lists/*

# Install cross-compilation toolchain for ARM64
ENV ARM_TOOLCHAIN_VERSION=11.2-2022.02
ENV ARM_TOOLCHAIN_NAME=gcc-arm-${ARM_TOOLCHAIN_VERSION}-x86_64-aarch64-none-linux-gnu
ENV ARM_TOOLCHAIN_PATH=/opt/arm-gnu-toolchain
ENV ARM_TOOLCHAIN_SYSROOT=$ARM_TOOLCHAIN_PATH/aarch64-none-linux-gnu/libc
ENV PATH="/usr/local/bin:$ARM_TOOLCHAIN_PATH/bin:$PATH"
RUN cd /tmp && mkdir -p $ARM_TOOLCHAIN_PATH && \
    wget -q https://developer.arm.com/-/media/Files/downloads/gnu/$ARM_TOOLCHAIN_VERSION/binrel/$ARM_TOOLCHAIN_NAME.tar.xz && \
    tar -xf $ARM_TOOLCHAIN_NAME.tar.xz --strip-components=1 -C $ARM_TOOLCHAIN_PATH && rm $ARM_TOOLCHAIN_NAME.tar.xz && \
    ln -s $ARM_TOOLCHAIN_PATH/bin/aarch64-none-linux-gnu-g++ /usr/local/bin/aarch64-linux-gnu-g++ && \
    ln -s $ARM_TOOLCHAIN_PATH/bin/aarch64-none-linux-gnu-gcc /usr/local/bin/aarch64-linux-gnu-gcc && \
    ln -s $ARM_TOOLCHAIN_PATH/bin/aarch64-none-linux-gnu-ar /usr/local/bin/aarch64-linux-gnu-ar

# Copy development headers to ARM64 sysroot
ENV ARM_SYSROOT_HEADERS="fontconfig freetype2 GL X11 KHR"
RUN mkdir -p $ARM_TOOLCHAIN_SYSROOT/usr/include && \
    for dir in $ARM_SYSROOT_HEADERS; do \
        cp -r /usr/include/$dir $ARM_TOOLCHAIN_SYSROOT/usr/include/ || exit 1; \
    done

# Use UTF-8 by default
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF
