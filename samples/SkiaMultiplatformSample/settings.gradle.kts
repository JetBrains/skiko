pluginManagement {
    repositories {
        mavenCentral {
            url = uri("https://cache-redirector.jetbrains.com/maven-central")
        }
        gradlePluginPortal()
        google()
        maven("https://redirector.kotlinlang.org/maven/compose-dev")
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        kotlin("jvm").version(kotlinVersion)
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
            library("skiko-ganesh", "org.jetbrains.skiko", "skiko-ganesh").versionRef("skiko")
            library("skiko-rendering", "org.jetbrains.skiko", "skiko-rendering").versionRef("skiko")

            val osName = System.getProperty("os.name")
            val hostOs = when {
                osName == "Mac OS X" -> "macos"
                osName.startsWith("Win") -> "windows"
                osName.startsWith("Linux") -> "linux"
                else -> error("Unsupported OS: $osName")
            }

            val osArch = System.getProperty("os.arch")
            var hostArch = when (osArch) {
                "x86_64", "amd64" -> "x64"
                "aarch64" -> "arm64"
                else -> error("Unsupported arch: $osArch")
            }

            library("skiko-awt-runtime", "org.jetbrains.skiko", "skiko-awt-runtime-$hostOs-$hostArch").versionRef("skiko")
            library("skiko-ganesh-awt-runtime", "org.jetbrains.skiko", "skiko-ganesh-awt-runtime-$hostOs-$hostArch").versionRef("skiko")
            library("skiko-rendering-awt-runtime", "org.jetbrains.skiko", "skiko-rendering-awt-runtime-$hostOs-$hostArch").versionRef("skiko")
        }
    }
}

rootProject.name = "SkiaMultiplatformSample"

if (extra.properties.getOrDefault("skiko.composite.build", "") == "1") {
    // Included builds don't inherit properties declared in this build's gradle.properties.
    // Enable all Skiko targets required by this multiplatform sample.
    System.setProperty("org.gradle.project.skiko.wasm.enabled", "true")
    System.setProperty("org.gradle.project.skiko.native.enabled", "true")

    includeBuild("../../skiko") {
        dependencySubstitution {
            substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
            substitute(module("org.jetbrains.skiko:skiko-ganesh")).using(project(":skiko-ganesh"))
            substitute(module("org.jetbrains.skiko:skiko-rendering")).using(project(":skiko-rendering"))
        }
    }
}
