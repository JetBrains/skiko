FROM ubuntu:focal
SHELL ["/bin/bash", "-c", "-l"]
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update -y && \
    apt-get install binutils build-essential software-properties-common -y && \
    apt-get install git curl wget unzip -y && \
    apt-get install python -y && \
    apt-get install pkg-config libdbus-1-dev -y && \
    apt-get install openjdk-11-jdk -y && \
    rm -rf /var/lib/apt/lists/*
ENV ANDROID_SDK_ROOT=/android/sdk
ARG CMD_TOOLS_VERSION=6858069
ARG CMD_TOOLS_ROOT=$ANDROID_SDK_ROOT/cmdline-tools/$CMD_TOOLS_VERSION
ARG SDK_MANAGER=$CMD_TOOLS_ROOT/bin/sdkmanager
RUN mkdir -p $CMD_TOOLS_ROOT && \
    export CMD_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-${CMD_TOOLS_VERSION}_latest.zip" && \
    wget $CMD_TOOLS_URL -O cmd-tools.zip && \
    unzip cmd-tools.zip && \
    rm cmd-tools.zip && \
    mv cmdline-tools/* $CMD_TOOLS_ROOT/
ARG ANDROID_PLATFORM=android-28
RUN yes | $SDK_MANAGER --licenses && \
    $SDK_MANAGER "platforms;$ANDROID_PLATFORM" && \
    cd $ANDROID_SDK_ROOT/platforms/$ANDROID_PLATFORM && \
    ls -1 | grep -v android.jar | xargs rm -rf
ARG NDK_VERSION=21.4.7075529
RUN $SDK_MANAGER "ndk;$NDK_VERSION" && \
    cd $ANDROID_SDK_ROOT/ndk/$NDK_VERSION && \
    ls -1 | grep -v toolchains | xargs rm -rf
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF
