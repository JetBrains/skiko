FROM ubuntu:20.04
ARG DEBIAN_FRONTEND=noninteractive

RUN apt update -y && \
    apt install binutils build-essential software-properties-common -y && \
    apt install zip unzip git python curl wget xvfb -y && \
    apt install fontconfig libfontconfig1-dev libglu1-mesa-dev libxrandr-dev libdbus-1-dev -y && \
    apt install pkg-config libwayland-dev libegl1-mesa-dev libxkbcommon-dev -y && \
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
    apt install libwayland-dev:arm64 libegl1-mesa-dev:arm64 libxkbcommon-dev:arm64 -y && \
    rm -rf /var/lib/apt/lists/*

# Vendor wayland-protocols: focal's package is 1.20, which predates the staging/
# protocols the skiko Wayland backend binds (fractional-scale-v1 needs >= 1.31).
# Only the XMLs and the pkg-config pkgdatadir variable are consumed (by skiko's
# generateWaylandProtocols task), so install the data files and a .pc by hand
# instead of pulling in meson for a full build.
ENV WAYLAND_PROTOCOLS_VERSION=1.45
RUN cd /tmp && \
    wget -q https://gitlab.freedesktop.org/wayland/wayland-protocols/-/releases/$WAYLAND_PROTOCOLS_VERSION/downloads/wayland-protocols-$WAYLAND_PROTOCOLS_VERSION.tar.xz && \
    tar -xf wayland-protocols-$WAYLAND_PROTOCOLS_VERSION.tar.xz && \
    mkdir -p /usr/local/share/wayland-protocols /usr/local/share/pkgconfig && \
    cp -r wayland-protocols-$WAYLAND_PROTOCOLS_VERSION/stable \
          wayland-protocols-$WAYLAND_PROTOCOLS_VERSION/staging \
          wayland-protocols-$WAYLAND_PROTOCOLS_VERSION/unstable \
          /usr/local/share/wayland-protocols/ && \
    printf 'prefix=/usr/local\ndatarootdir=${prefix}/share\npkgdatadir=${datarootdir}/wayland-protocols\n\nName: Wayland Protocols\nDescription: Wayland protocol files\nVersion: %s\n' "$WAYLAND_PROTOCOLS_VERSION" \
        > /usr/local/share/pkgconfig/wayland-protocols.pc && \
    rm -rf wayland-protocols-$WAYLAND_PROTOCOLS_VERSION*

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
ENV ARM_SYSROOT_HEADERS="fontconfig freetype2 GL X11 KHR"
RUN mkdir -p $ARM_TOOLCHAIN_SYSROOT/usr/include && \
    for dir in $ARM_SYSROOT_HEADERS; do \
        cp -r /usr/include/$dir $ARM_TOOLCHAIN_SYSROOT/usr/include/ || exit 1; \
    done

# Android SDK for :skiko:dokkaHtml (skiko.android.enabled needs android.jar from
# compileSdk 35). No NDK: the Android target declares no externalNativeBuild, so
# docs generation never invokes it. Kept last so it doesn't invalidate the cache
# of the layers above.
ENV ANDROID_SDK_ROOT=/android/sdk
RUN cd /tmp && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip -O cmd-tools.zip && \
    unzip -q cmd-tools.zip && rm cmd-tools.zip && \
    yes | cmdline-tools/bin/sdkmanager --sdk_root=$ANDROID_SDK_ROOT --licenses && \
    cmdline-tools/bin/sdkmanager --sdk_root=$ANDROID_SDK_ROOT "platforms;android-35" && \
    rm -rf cmdline-tools

# Use UTF-8 by default
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF
