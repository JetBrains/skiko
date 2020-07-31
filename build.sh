#!/bin/bash

./preBuild.sh
cd skiko && ./gradlew publishToMavenLocal
