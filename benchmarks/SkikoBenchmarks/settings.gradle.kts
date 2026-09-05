pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral {
            url = uri("https://cache-redirector.jetbrains.com/maven-central")
        }
        gradlePluginPortal()
        google()
    }
    plugins {
        val kotlinVersion = providers.gradleProperty("kotlin.version").get()
        kotlin("multiplatform").version(kotlinVersion)
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val skikoVersion = providers.gradleProperty("skiko.version").orNull
                ?: "unspecified"

            version("skiko", skikoVersion)
            version("kotlinxBrowser", "0.5.0")

            library("skiko", "org.jetbrains.skiko", "skiko").versionRef("skiko")
            library("skiko-js-wasm-runtime", "org.jetbrains.skiko", "skiko-js-wasm-runtime").versionRef("skiko")
            library("skiko-wasm-js", "org.jetbrains.skiko", "skiko-wasm-js").versionRef("skiko")
            library("browser", "org.jetbrains.kotlinx", "kotlinx-browser").versionRef("kotlinxBrowser")
        }
    }
}

rootProject.name = "SkikoBenchmarks"

val useIncludedSkiko = extra.properties.getOrDefault("skiko.composite.build", "") == "1" ||
    providers.gradleProperty("skiko.version").orNull == null

if (useIncludedSkiko) {
    includeBuild("../../skiko") {
        dependencySubstitution {
            substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
        }
    }
}
