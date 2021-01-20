docker run --rm --mount type=bind,source=$HOME/compose,target=/host -ti skiko-build bash -c "cd /host/skiko/skiko && ./gradlew skikoJvmRuntimeJar && bash"

