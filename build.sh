#!/bin/bash

if [ ! -d ./skija ]; then
 git clone https://github.com/JetBrains/skija.git
else
 pushd ./skija && git pull && popd
fi

java -jar ./libs/lombok.jar delombok skija/src/main/java -d  skija/java_delombok --classpath=./libs/annotations-19.0.0.jar:./libs/lombok.jar

cd skiko && ./gradlew publishSkikoPublicationToMavenLocal
