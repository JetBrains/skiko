FROM ubuntu:focal-20211006
SHELL ["/bin/bash", "-c", "-l"]
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update -y && \
    apt-get install binutils build-essential software-properties-common -y && \
    apt-get install git curl wget -y && \
    apt-get install python -y && \
    apt-get install openjdk-11-jdk -y
# Install libs & tools
RUN apt-get install git python wget -y && \
    apt-get install fontconfig libfontconfig1-dev libglu1-mesa-dev libxrandr-dev libdbus-1-dev curl zip -y
ENV EMSDK_DIR=/usr/emsdk
ENV EMSDK_VER=3.1.50
RUN git clone https://github.com/emscripten-core/emsdk.git $EMSDK_DIR && \
    $EMSDK_DIR/emsdk install $EMSDK_VER && \
    $EMSDK_DIR/emsdk activate $EMSDK_VER
ENV PATH=$EMSDK_DIR/upstream/emscripten:$PATH
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF
