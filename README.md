# Kotlin Multiplatform bindings to Skia

## Building JVM bindings

* (Optional) Set SKIA_DIR to location of Skia (binaries can be downloaded 
[here](https://bintray.com/beta/#/jetbrains/skija/Skia?tab=files))
* Set JAVA_HOME to location of JDK, at least version 11
* `cd skiko && ./gradlew publishToMavenLocal` will build the artefact and publish it to local Maven repo
