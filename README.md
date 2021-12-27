[![official project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![version](https://img.shields.io/badge/dynamic/json.svg?color=orange&label=latest%20version&query=%24.tag_name&url=https%3A%2F%2Fgithub.com%2FJetBrains%2Fskiko%2Freleases%2Flatest)](https://github.com/JetBrains/skiko/releases/latest)
# Kotlin Multiplatform library for Skia and window management #

Skiko (short for Skia for Kotlin) is the graphical library exposing significant part
of [Skia library](https://skia.org) APIs to Kotlin, along with the gluing code for rendering context.
Linux(x86_64 and arm64), Windows(x86_64) and macOS(x86_64 and arm64) builds for Kotlin/JVM are available.
JS + WebAssembly build for Kotlin/JS are available.
iOS arm64/x64 and macOS arm64/x64 builds for Kotlin/Native are available.

## Using as dependency

To use in build scripts one has to compute appropriate target platform and version,
i.e. something like this

```kotlin
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    val osName = System.getProperty("os.name")
    val targetOs = when {
        osName == "Mac OS X" -> "macos"
        osName.startsWith("Win") -> "windows"
        osName.startsWith("Linux") -> "linux"
        else -> error("Unsupported OS: $osName")
    }

    val osArch = System.getProperty("os.arch")
    var targetArch = when (osArch) {
        "x86_64", "amd64" -> "x64"
        "aarch64" -> "arm64"
        else -> error("Unsupported arch: $osArch")
    }

    val version = "0.5.3"
    val target = "${targetOs}-${targetArch}"
    dependencies {
        implementation("org.jetbrains.skiko:skiko-awt-runtime-$target:$version")
    }
```

Simple example for Kotlin/JVM
```kotlin
fun main() {
    val skiaLayer = SkiaLayer()
    skiaLayer.skikoView = GenericSkikoView(skiaLayer, object : SkikoView {
        val paint = Paint().apply {
            color = Color.RED
        }
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            canvas.clear(Color.CYAN)
            val ts = nanoTime / 5_000_000
            canvas.drawCircle( (ts % width).toFloat(), (ts % height).toFloat(), 20f, paint )
        }
    })
    SwingUtilities.invokeLater {
        val window = JFrame("Skiko example").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            preferredSize = Dimension(800, 600)
        }
        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRedraw()
        window.pack()
        window.isVisible = true
    }
}
```

To use latest development snapshot use version `0.0.0-SNAPSHOT`.

## Building JVM bindings

* Set JAVA_HOME to location of JDK, at least version 11
* `./gradlew :skiko:publishToMavenLocal` will build the artifact and publish it to local Maven repo

To build with debug symbols and debug Skia build use `-Pskiko.debug=true` Gradle argument.

#### Working with Skia sources

Gradle build downloads the necessary version of Skia by default.
However, if downloaded sources are modified, changes are discarded (Gradle
re-evaluates tasks, when outputs are changed).
To use custom version of the dependencies, specify `SKIA_DIR` environment variable.

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

#### Running UI tests
Add `-Dskiko.test.ui.enabled=true` to enable UI tests (integration tests, which run in the native window). Each UI test will be run on every available Graphics API of the current target.

For example, if we want to include UI tests when we test JVM target, call this:
```
./gradlew awtTest -Dskiko.test.ui.enabled=true
```
Don't run any background tasks, click mouse, or press keys during the tests. Otherwise, they probably fail.

#### Run samples
 - First follow the instruction here: [Building JVM bindings](#building-jvm-bindings)
 - `./gradlew :SkiaJvmSample:run`  
