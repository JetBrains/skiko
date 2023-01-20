pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
        google()
    }
    
}
rootProject.name = "SkiaMultiplatformSample"

if (System.getenv("SKIKO_COMPOSITE_BUILD") == "1") {
    includeBuild("../../skiko") {
        dependencySubstitution {
            substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
        }
    }
}
