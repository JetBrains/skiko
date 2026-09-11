pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral {
            url = uri("https://cache-redirector.jetbrains.com/maven-central")
        }
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

// Define version catalog programmatically so we can read versions from gradle.properties
// This overrides the automatic import of gradle/libs.versions.toml for the "libs" catalog.
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("skiko", providers.gradleProperty("skiko.version").get())
            version("kotlinxBrowser", "0.5.0")

            library("skiko", "org.jetbrains.skiko", "skiko").versionRef("skiko")
            library("skiko-rendering", "org.jetbrains.skiko", "skiko-rendering").versionRef("skiko")
            library("browser", "org.jetbrains.kotlinx", "kotlinx-browser").versionRef("kotlinxBrowser")
        }
    }
}

rootProject.name = "SkiaWebSample"

if (extra.properties.getOrDefault("skiko.composite.build", "") == "1") {
    // Included builds don't inherit properties declared in this build's gradle.properties.
    // Skiko disables web targets by default, so enable them for this web sample.
    System.setProperty("org.gradle.project.skiko.wasm.enabled", "true")

    includeBuild("../../skiko") {
        dependencySubstitution {
            substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
            substitute(module("org.jetbrains.skiko:skiko-rendering")).using(project(":skiko-rendering"))
        }
    }
}
