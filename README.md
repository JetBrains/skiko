# Kotlin Multiplatform bindings to Skia

## Building JVM bindings

   * Download or build Skia for your target platform (see https://bintray.com/beta/#/jetbrains/skija/Skia?tab=files)
   * Set SKIA_DIR to location of Skia
   * Set JAVA_HOME to location of JDK, at least version 11
   * Run `build.sh` at least once to properly set up Skija
   * If Skija repo is updated - run `build.sh` again
   * `cd skiko && ./gradlew publishSkikoPublicationToMavenLocal` will build the artefact and publish it to local Maven repo
  
