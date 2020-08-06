#!/bin/bash

if [ ! -d ./skija ]; then
  git clone https://github.com/JetBrains/skija.git
  pushd ./skija
else
  pushd ./skija && git pull
fi
git checkout c3faf18878a0ad3244e4119a69b555b5b77a21d0
popd

SEP=:
if [ ! -z `uname | grep MINGW` ]; then
  SEP=\;
fi

java -jar ./libs/lombok.jar delombok skija/src/main/java -d skiko/src/jvmMain/java \
   --classpath=./libs/annotations-19.0.0.jar${SEP}./libs/lombok.jar
