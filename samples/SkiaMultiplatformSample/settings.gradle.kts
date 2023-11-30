pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        kotlin("jvm").version(kotlinVersion)
        kotlin("multiplatform").version(kotlinVersion)
    }

}
rootProject.name = "SkiaMultiplatformSample"

if (extra.properties.getOrDefault("skiko.composite.build", "") == "1") {
    includeBuild("../../skiko") {
        dependencySubstitution {
            substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
        }
    }
}
