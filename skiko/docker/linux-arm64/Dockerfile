FROM arm64v8/ubuntu:20.04
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update -y
RUN apt-get install binutils build-essential -y
RUN apt-get install software-properties-common -y
RUN apt-get install python git fontconfig libfontconfig1-dev libglu1-mesa-dev curl wget -y
RUN apt-get install clang-11 -y && \
    apt-get remove g++ -y && \
    update-alternatives --install /usr/bin/clang clang /usr/bin/clang-11 100 && \
    update-alternatives --install /usr/bin/clang++ clang++ /usr/bin/clang++-11 100

# Install libs & tools
ENV DEPOT_TOOLS=/usr/depot_tools
ENV PATH=$DEPOT_TOOLS:$PATH
RUN git clone 'https://chromium.googlesource.com/chromium/tools/depot_tools.git' $DEPOT_TOOLS

# Install Java
ENV JAVA_HOME=/usr/java/11
ENV PATH=$JAVA_HOME/bin:$PATH
RUN JAVA_URL=https://corretto.aws/downloads/latest/amazon-corretto-11-aarch64-linux-jdk.tar.gz && \
    JAVA_ARCHIVE=/tmp/jdk.tar.gz && \
    JAVA_BASE=/usr/java/ && \
    wget $JAVA_URL --output-document $JAVA_ARCHIVE && \
    mkdir -p $JAVA_BASE && \
    tar -xzf $JAVA_ARCHIVE --directory $JAVA_BASE && \
    find $JAVA_BASE -type d -maxdepth 1 -name "amazon-corretto-11*linux-aarch64" -exec mv {} $JAVA_HOME \; && \
    rm $JAVA_ARCHIVE
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF