FROM arm64v8/ubuntu:20.04
ARG DEBIAN_FRONTEND=noninteractive

RUN apt update -y && \
    apt install binutils build-essential software-properties-common -y && \
    apt install zip unzip git python curl wget xvfb -y && \
    apt install fontconfig libfontconfig1-dev libglu1-mesa-dev libxrandr-dev libdbus-1-dev -y && \
    apt install openjdk-21-jdk -y && \
    rm -rf /var/lib/apt/lists/*

# Install chromium tools
ENV DEPOT_TOOLS=/usr/depot_tools
ENV PATH=$DEPOT_TOOLS:$PATH
RUN git clone 'https://chromium.googlesource.com/chromium/tools/depot_tools.git' $DEPOT_TOOLS

# Use UTF-8 by default
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF
