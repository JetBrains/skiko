#!/bin/bash

if [ ! -d ./skija ]; then
  git clone https://github.com/JetBrains/skija.git
  pushd ./skija
else
  pushd ./skija && git pull
fi
git checkout 82b8c697643ae8f2d6a315c27bd47601533b0397
popd

SEP=:
if [ ! -z `uname | grep MINGW` ]; then
  SEP=\;
fi

java -jar ./libs/lombok.jar delombok skija/src/main/java -d skiko/src/jvmMain/java \
   --classpath=./libs/annotations-19.0.0.jar${SEP}./libs/lombok.jar