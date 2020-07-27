#!/bin/bash

if [ ! -d ./skija ]; then
 git clone https://github.com/JetBrains/skija.git
else
 pushd ./skija && git pull && popd
fi

java -jar ./libs/lombok.jar delombok skija/src/main/java -d skiko/src/jvmMain/java --classpath=./libs/annotations-19.0.0.jar:./libs/lombok.jar
mkdir -p skiko/src/main
rsync -r skija/src/main/cc/ skiko/src/main/cpp

cd skiko && ./gradlew jar
