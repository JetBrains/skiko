pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
    
}
rootProject.name = "SkiaMultiplatformSample"

includeBuild("../../skiko") {
    dependencySubstitution {
        substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
    }
}
