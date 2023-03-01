## Building JVM bindings

* Install Xcode Command Line Tools
* Install Emscripten
* Set JAVA_HOME to location of JDK, at least version 11
* `./gradlew :skiko:publishToMavenLocal` will build the artifact and publish it to local Maven repo

To build with debug symbols and debug Skia build use `-Pskiko.debug=true` Gradle argument.

#### Working with Skia sources

Gradle build downloads the necessary version of Skia by default.
However, if downloaded sources are modified, changes are discarded (Gradle
re-evaluates tasks, when outputs are changed).
To use custom version of the dependencies, specify `SKIA_DIR` environment variable.

#### Building on Windows

##### Using Visual Studio Build Tools 2019
1. Download [Visual Studio Build Tools 2019](https://learn.microsoft.com/en-us/visualstudio/releases/2019/history) (search "BuildTools" on the page).
2. During the installation, select "Desktop development with C++"
3. Add an environment variable SKIKO_VSBT_PATH=C:/Program Files (x86)/Microsoft Visual Studio/2019/BuildTools
```
Control Panel|All Control Panel Items|System|Advanced system settings|Environment variables
```
or by running `cmd` as administrator:
```
setx /M SKIKO_VSBT_PATH "C:\Program Files (x86)\Microsoft Visual Studio\2019\BuildTools"
```

#### Running UI tests
Add `-Dskiko.test.ui.enabled=true` to enable UI tests (integration tests, which run in the native window). Each UI test will be run on every available Graphics API of the current target.

For example, if we want to include UI tests when we test JVM target, call this:
```
./gradlew awtTest -Dskiko.test.ui.enabled=true
```
Don't run any background tasks, click mouse, or press keys during the tests. Otherwise, they probably fail.

#### Run samples
- First follow the instruction here: [Building JVM bindings](#building-jvm-bindings)
- `./gradlew :SkiaAwtSample:run`
