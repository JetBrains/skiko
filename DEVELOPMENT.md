### Building JVM bindings

* Prepare the system
  * `macOs` Install Xcode Command Line Tools
  * `Linux` Install these tools:
    ```
    sudo apt-get install ninja-build fontconfig libfontconfig1-dev libglu1-mesa-dev libxrandr-dev libdbus-1-dev zip libx11-dev
    ```
  * `Windows`
    1. Download [LLVM](https://github.com/llvm/llvm-project/releases/tag/llvmorg-17.0.1) (search "LLVM-version-x64.exe" on the page).
    2. Add the "bin" folder into your PATH
    ```
    Control Panel|All Control Panel Items|System|Advanced system settings|Environment variables
    ```

* Install Emscripten
* Set `JAVA_HOME` to location of JDK, at least version 11
* `./gradlew :skiko:publishToMavenLocal` will build the artifact and publish it to local Maven repo

To build with debug symbols and debug Skia build use `-Pskiko.debug=true` Gradle argument.

#### Working with Skia sources

Gradle build downloads the necessary version of Skia by default.
However, if downloaded sources are modified, changes are discarded (Gradle
re-evaluates tasks, when outputs are changed).
To use custom version of the dependencies, specify `SKIA_DIR` environment variable.

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
