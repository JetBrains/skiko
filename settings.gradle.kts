rootProject.name = "skiko-all"
includeBuild("samples/SkiaAwtSample")
includeBuild("samples/SkiaMultiplatformSample")
includeBuild("skiko") {
    dependencySubstitution {
        substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
    }
}
include("shared")
include("iosApp")
