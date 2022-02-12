FROM ubuntu:focal-20211006
SHELL ["/bin/bash", "-c", "-l"]
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update -y && \
    apt-get install binutils build-essential software-properties-common libdbus-1-dev -y && \
    apt-get install git curl wget -y && \
    apt-get install python -y && \
    apt-get install openjdk-11-jdk -y
ENV EMSDK_DIR=/usr/emsdk
ENV EMSDK_VER=2.0.29
RUN git clone https://github.com/emscripten-core/emsdk.git $EMSDK_DIR && \
    $EMSDK_DIR/emsdk install $EMSDK_VER && \
    $EMSDK_DIR/emsdk activate $EMSDK_VER
ENV PATH=$EMSDK_DIR/upstream/emscripten:$PATH
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF
