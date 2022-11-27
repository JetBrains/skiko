rootProject.name = "skiko-all"
includeBuild("samples/SkiaAwtSample")
includeBuild("skiko") {
    dependencySubstitution {
        substitute(module("org.jetbrains.skiko:skiko-awt")).using(project(":"))
    }
}
