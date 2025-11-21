FROM ubuntu:20.04
ARG DEBIAN_FRONTEND=noninteractive

RUN apt update -y && \
    apt install binutils build-essential software-properties-common -y && \
    apt install zip unzip git python curl wget -y && \
    apt install fontconfig libfontconfig1-dev libglu1-mesa-dev libxrandr-dev libdbus-1-dev -y && \
    apt install openjdk-21-jdk -y && \
    rm -rf /var/lib/apt/lists/*

# Install emscripten
ENV EMSDK_DIR=/usr/emsdk
ENV EMSDK_VER=3.1.50
ENV PATH=$EMSDK_DIR/upstream/emscripten:$PATH
RUN git clone https://github.com/emscripten-core/emsdk.git $EMSDK_DIR && \
    $EMSDK_DIR/emsdk install $EMSDK_VER && \
    $EMSDK_DIR/emsdk activate $EMSDK_VER

# Use UTF-8 by default
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF
