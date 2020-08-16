# Kotlin Multiplatform bindings to Skia

## Building JVM bindings

* (Optional) Set SKIA_DIR to location of Skia (binaries can be downloaded 
[here](https://bintray.com/beta/#/jetbrains/skija/Skia?tab=files))
* Set JAVA_HOME to location of JDK, at least version 11
* `cd skiko && ./gradlew publishToMavenLocal` will build the artefact and publish it to local Maven repo


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