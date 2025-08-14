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

// Define version catalog programmatically so we can read versions from gradle.properties
// This overrides the automatic import of gradle/libs.versions.toml for the "libs" catalog.
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("skiko", providers.gradleProperty("skiko.version").get())
            library("skiko", "org.jetbrains.skiko", "skiko").versionRef("skiko")
            library("skiko-runtime", "org.jetbrains.skiko", "skiko-js-wasm-runtime").versionRef("skiko")
        }
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
