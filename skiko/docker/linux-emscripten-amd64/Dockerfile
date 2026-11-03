FROM node:24.22.1-bullseye-slim AS node
FROM ubuntu:22.04
ARG DEBIAN_FRONTEND=noninteractive

RUN apt update -y && \
    apt install binutils build-essential software-properties-common -y && \
    apt install zip unzip git python3 curl wget -y && \
    apt install fontconfig libfontconfig1-dev libglu1-mesa-dev libxrandr-dev libdbus-1-dev -y && \
    apt install libvulkan1 libvulkan-dev mesa-vulkan-drivers -y && \
    apt install openjdk-21-jdk -y && \
    rm -rf /var/lib/apt/lists/*

# Install emscripten
ENV EMSDK_DIR=/usr/emsdk
ENV EMSDK_VER=4.0.7
ENV PATH=$EMSDK_DIR/upstream/emscripten:$PATH
RUN git clone https://github.com/emscripten-core/emsdk.git $EMSDK_DIR && \
    $EMSDK_DIR/emsdk install $EMSDK_VER && \
    $EMSDK_DIR/emsdk activate $EMSDK_VER

# Use different Node to fix eMudhra certificate issue (https://youtrack.jetbrains.com/issue/CMP-11011)
COPY --from=node /usr/local/bin/node /usr/local/bin/node
ENV EM_NODE_JS=/usr/local/bin/node
RUN rm -rf "$EMSDK_DIR/node" && node --version

# Use UTF-8 by default
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF
