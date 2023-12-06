pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
        }
    }
    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        kotlin("multiplatform").version(kotlinVersion)
    }

}
rootProject.name = "SkiaJsSample"

if (extra.properties.getOrDefault("skiko.composite.build", "") == "1") {
    includeBuild("../../skiko") {
        dependencySubstitution {
            substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
        }
    }
}
