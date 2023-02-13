FROM arm64v8/ubuntu:18.04
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
    apt-get install binutils build-essential -y --no-install-recommends && \
    apt-get install software-properties-common -y --no-install-recommends && \
    apt-get install fontconfig libfontconfig1-dev libglu1-mesa-dev libxrandr-dev libdbus-1-dev -y --no-install-recommends && \
    apt-get install python git curl wget -y --no-install-recommends && \
    apt-get install openjdk-11-jdk -y --no-install-recommends && \
    apt-get install clang-9 -y --no-install-recommends && \
    update-alternatives --install /usr/bin/clang clang /usr/bin/clang-9 100 && \
    update-alternatives --install /usr/bin/clang++ clang++ /usr/bin/clang++-9 100

# Install libs & tools
ENV DEPOT_TOOLS=/usr/depot_tools
ENV PATH=$DEPOT_TOOLS:$PATH
RUN git clone 'https://chromium.googlesource.com/chromium/tools/depot_tools.git' $DEPOT_TOOLS

# Use UTF-8 by default
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF