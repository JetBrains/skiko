[![official project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![version](https://img.shields.io/badge/dynamic/json.svg?color=orange&label=latest%20version&query=%24.tag_name&url=https%3A%2F%2Fgithub.com%2FJetBrains%2Fskiko%2Freleases%2Flatest)](https://github.com/JetBrains/skiko/releases/latest)
# Kotlin Multiplatform bindings to Skia #

Skiko (short for Skia for Kotlin) is the graphical library exposing significant part
of [Skia library](https://skia.org) APIs to Kotlin, along with the gluing code for rendering context.
At the moment, Linux(x86_64), Windows(x86_64) and macOS(x86_64) builds for Kotlin/JVM are available.
Using the library from Kotlin is as simple as:
```kotlin
fun main(args: Array<String>) {
    SkiaWindow().apply {
        layer.renderer = Renderer { renderer, w, h ->
            val canvas = renderer.canvas!!
            val paint1 = Paint().setColor(0xffff0000.toInt()) // ARGB
            canvas.drawRRect(RRect.makeLTRB(10f, 10f, w - 10f, h - 10f, 5f), paint1)
            val paint2 = Paint().setColor(0xff00ff00.toInt()) // ARGB
            canvas.drawRRect(RRect.makeLTRB(30f, 30f, w - 30f, h - 30f, 10f), paint2)

        }
        setVisible(true)
        setSize(800, 600)
    }
}

class Renderer(val displayScene: (Renderer, Int, Int) -> Unit): SkiaRenderer {
    val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    val font = Font(typeface, 40f)
    val paint = Paint().apply {
            setColor(0xff9BC730L.toInt())
            setMode(PaintMode.FILL)
            setStrokeWidth(1f)
    }

    var canvas: Canvas? = null

    override fun onInit() {
    }

    override fun onDispose() {
    }

    override fun onReshape(width: Int, height: Int) {
    }

    override fun onRender(canvas: Canvas, width: Int, height: Int) {
        this.canvas = canvas
        displayScene(this, width, height)
    }
}
```
With the following `build.gradle`:
```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.3.72'
    id 'application'
}

repositories {
    mavenLocal()
    jcenter()
    maven {
       url 'https://packages.jetbrains.team/maven/p/ui/dev'
    }
}

def os = System.getProperty("os.name")
def target = ""
if (os == "Mac OS X") {
    target = "macos"
} else if (os.startsWith("Win")) {
    target = "windows"
} else if (os.startsWith("Linux")) {
    target = "linux"
} else {
    throw Error("Unsupported OS: $target")
}

dependencies {
    implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8'
    implementation "org.jetbrains.skiko:skiko-jvm-runtime-$target:0.1.5"
}

application {
    mainClassName = 'AppKt'
}
```

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
