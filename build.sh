#!/bin/bash

if [ ! -d ./skija ]; then
 git clone https://github.com/JetBrains/skija.git
else
 cd ./skija && git pull
fi

java -jar lombok.jar delombok skija/src/main/java -d skiko/src/jvmMain/java --classpath=./annotations-19.0.0.jar:./lombok.jar
mkdir -p skiko/src/main
rsync -r skija/src/main/cc/ skiko/src/main/cpp

cd skiko && ./gradlew build
