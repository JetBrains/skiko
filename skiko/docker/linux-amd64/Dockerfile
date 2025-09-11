FROM ubuntu:20.04
ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update -y && \
    apt-get install binutils build-essential software-properties-common -y && \
    apt-get install zip unzip git python curl wget -y && \
    apt-get install fontconfig libfontconfig1-dev libglu1-mesa-dev libxrandr-dev libdbus-1-dev multistrap -y && \
    apt-get install openjdk-21-jdk -y && \
    apt-get install gcc-9 g++-9 -y && \
    update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-9 60 --slave /usr/bin/g++ g++ /usr/bin/g++-9 && \
    update-alternatives --config gcc && \
    apt-get install gcc-9-aarch64-linux-gnu g++-9-aarch64-linux-gnu -y && \
    update-alternatives --install /usr/bin/aarch64-linux-gnu-gcc aarch64-linux-gnu-gcc /usr/bin/aarch64-linux-gnu-gcc-9 60 --slave /usr/bin/aarch64-linux-gnu-g++ aarch64-linux-gnu-g++ /usr/bin/aarch64-linux-gnu-g++-9 && \
    update-alternatives --config aarch64-linux-gnu-gcc && \
    rm -rf /var/lib/apt/lists/*

# Install chromium tools
ENV DEPOT_TOOLS=/usr/depot_tools
ENV PATH=$DEPOT_TOOLS:$PATH
RUN git clone 'https://chromium.googlesource.com/chromium/tools/depot_tools.git' $DEPOT_TOOLS

# Use UTF-8 by default
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF
