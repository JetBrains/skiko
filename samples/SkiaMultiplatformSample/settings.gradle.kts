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

// Define version catalog programmatically so we can read versions from gradle.properties
// This overrides the automatic import of gradle/libs.versions.toml for the "libs" catalog.
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("skiko", providers.gradleProperty("skiko.version").get())
            library("skiko", "org.jetbrains.skiko", "skiko").versionRef("skiko")
            library("skiko-wasm-runtime", "org.jetbrains.skiko", "skiko-js-wasm-runtime").versionRef("skiko")

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
        }
    }
}

rootProject.name = "SkiaMultiplatformSample"

val compositeBuildProp = providers.gradleProperty("skiko.composite.build").orNull
val isLinuxHost = System.getProperty("os.name").startsWith("Linux")
val useCompositeBuild = when (compositeBuildProp) {
    "1" -> true
    "0" -> false
    else -> isLinuxHost && file("../../skiko").exists()
}

if (useCompositeBuild) {
    includeBuild("../../skiko") {
        dependencySubstitution {
            substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
        }
    }
}
