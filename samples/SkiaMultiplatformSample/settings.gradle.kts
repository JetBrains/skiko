pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
    
}
rootProject.name = "SkiaMultiplatformSample"

if (System.getenv("SKIKO_COMPOSITE_BUILD") == "1" || System.getProperty("skikoCompositeBuild") == "true") {
    includeBuild("../../skiko") {
        dependencySubstitution {
            substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
        }
    }
}
