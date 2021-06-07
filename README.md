[![official project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![version](https://img.shields.io/badge/dynamic/json.svg?color=orange&label=latest%20version&query=%24.tag_name&url=https%3A%2F%2Fgithub.com%2FJetBrains%2Fskiko%2Freleases%2Flatest)](https://github.com/JetBrains/skiko/releases/latest)
# Kotlin Multiplatform library for Skia and window management #

Skiko (short for Skia for Kotlin) is the graphical library exposing significant part
of [Skia library](https://skia.org) APIs to Kotlin, along with the gluing code for rendering context.
At the moment, Linux(x86_64), Windows(x86_64) and macOS(x86_64 and arm64) builds for Kotlin/JVM are available.

## Building JVM bindings

* (Optional) Set SKIA_DIR to location of Skia (binaries can be downloaded 
[here](https://bintray.com/beta/#/jetbrains/skija/Skia?tab=files))
* Set JAVA_HOME to location of JDK, at least version 11
* `cd skiko && ./gradlew publishToMavenLocal` will build the artefact and publish it to local Maven repo

#### Working with Skia or Skija sources

Gradle build downloads the necessary version of Skia & Skija by default.
However, if downloaded sources are modified, changes are discarded (Gradle
re-evaluates tasks, when outputs are changed).
To use custom version of the dependencies, specify `SKIA_DIR` or `SKIJA_DIR` environment variable
respectively.

#### Building on Windows

##### Using Visual Studio C++
* Install [Visual Studio Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/) or
[Visual Studio C++](https://visualstudio.microsoft.com/vs/features/cplusplus/).
* If Gradle fails to find Visual C++ toolchain:
```
Execution failed for task ':compileDebugWindowsCpp'.
> No tool chain is available to build C++ for host operating system 'Windows 10' architecture 'x86-64':
    - Tool chain 'visualCpp' (Visual Studio):
        - Could not locate a Visual Studio installation, using the command line tool, Windows registry or system path.
```
set `SKIKO_VSBT_PATH` environment variable to the location of installed Visual Studio Build Tools. 
This could be done in the UI:
```
Control Panel|All Control Panel Items|System|Advanced system settings|Environment variables
```
or by running `cmd` as administrator:
```
setx /M SKIKO_VSBT_PATH "C:\Program Files (x86)\Microsoft Visual Studio\2019\BuildTools"
```
