FROM ubuntu:20.04
ARG DEBIAN_FRONTEND=noninteractive

RUN apt update -y && \
    apt install binutils build-essential software-properties-common -y && \
    apt install zip unzip git python curl wget xvfb -y && \
    apt install fontconfig libfontconfig1-dev libglu1-mesa-dev libxrandr-dev libdbus-1-dev -y && \
    apt install openjdk-21-jdk -y && \
    apt install gcc-10 g++-10 -y && \
    update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-10 60 --slave /usr/bin/g++ g++ /usr/bin/g++-10 && \
    update-alternatives --config gcc && \
    rm -rf /var/lib/apt/lists/*

# Install chromium tools
ENV DEPOT_TOOLS=/usr/depot_tools
ENV PATH=$DEPOT_TOOLS:$PATH
RUN git clone 'https://chromium.googlesource.com/chromium/tools/depot_tools.git' $DEPOT_TOOLS

# Install ARM64 libraries for cross-compilation
RUN dpkg --add-architecture arm64 && echo \
        "deb [arch=arm64] http://ports.ubuntu.com/ focal main restricted\n" \
        "deb [arch=arm64] http://ports.ubuntu.com/ focal-updates main restricted\n" \
        "deb [arch=arm64] http://ports.ubuntu.com/ focal universe\n" \
        "deb [arch=arm64] http://ports.ubuntu.com/ focal-updates universe\n" \
        "deb [arch=arm64] http://ports.ubuntu.com/ focal multiverse\n" \
        "deb [arch=arm64] http://ports.ubuntu.com/ focal-updates multiverse\n" \
        "deb [arch=arm64] http://ports.ubuntu.com/ focal-backports main restricted universe multiverse" \
    | tee /etc/apt/sources.list.d/arm-cross-compile-sources.list && \
    sed -i -E "s/(deb)\ (http:.+)/\1\ [arch=amd64]\ \2/" /etc/apt/sources.list && \
    apt update -y && \
    apt install libfontconfig1-dev:arm64 libglu1-mesa-dev:arm64 libxrandr-dev:arm64 libdbus-1-dev:arm64 -y && \
    rm -rf /var/lib/apt/lists/*

# Install cross-compilation toolchain for ARM64
ENV ARM_TOOLCHAIN_VERSION=10.3-2021.07
ENV ARM_TOOLCHAIN_NAME=gcc-arm-${ARM_TOOLCHAIN_VERSION}-x86_64-aarch64-none-linux-gnu
ENV ARM_TOOLCHAIN_PATH=/opt/arm-gnu-toolchain
ENV ARM_TOOLCHAIN_SYSROOT=$ARM_TOOLCHAIN_PATH/aarch64-none-linux-gnu/libc
ENV PATH="/usr/local/bin:$ARM_TOOLCHAIN_PATH/bin:$PATH"
RUN cd /tmp && mkdir -p $ARM_TOOLCHAIN_PATH && \
    wget -q https://developer.arm.com/-/media/Files/downloads/gnu-a/$ARM_TOOLCHAIN_VERSION/binrel/$ARM_TOOLCHAIN_NAME.tar.xz && \
    tar -xf $ARM_TOOLCHAIN_NAME.tar.xz --strip-components=1 -C $ARM_TOOLCHAIN_PATH && rm $ARM_TOOLCHAIN_NAME.tar.xz && \
    ln -s $ARM_TOOLCHAIN_PATH/bin/aarch64-none-linux-gnu-g++ /usr/local/bin/aarch64-linux-gnu-g++ && \
    ln -s $ARM_TOOLCHAIN_PATH/bin/aarch64-none-linux-gnu-gcc /usr/local/bin/aarch64-linux-gnu-gcc && \
    ln -s $ARM_TOOLCHAIN_PATH/bin/aarch64-none-linux-gnu-ar /usr/local/bin/aarch64-linux-gnu-ar

# Copy development headers to ARM64 sysroot
RUN mkdir -p $ARM_TOOLCHAIN_SYSROOT/usr/include && \
    cp -r /usr/include/fontconfig $ARM_TOOLCHAIN_SYSROOT/usr/include/ && \
    cp -r /usr/include/freetype2 $ARM_TOOLCHAIN_SYSROOT/usr/include/ && \
    cp -r /usr/include/GL $ARM_TOOLCHAIN_SYSROOT/usr/include/ && \
    cp -r /usr/include/X11 $ARM_TOOLCHAIN_SYSROOT/usr/include/ && \
    cp -r /usr/include/KHR $ARM_TOOLCHAIN_SYSROOT/usr/include/

# Use UTF-8 by default
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF
