pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()

        // TODO: delete when we have all libs in mavenCentral
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
    buildscript {
        repositories {
            mavenCentral()
            maven("https://maven.pkg.jetbrains.space/public/p/compose/internal")
            maven("https://maven.pkg.jetbrains.space/public/p/space/maven")

            // TODO: delete when we have all libs in mavenCentral
            maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        }
        dependencies {
            classpath("org.jetbrains.compose.internal.build-helpers:publishing:0.1.3")
            classpath("org.kohsuke:github-api:1.116")
        }
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        kotlin("jvm").version(kotlinVersion)
        kotlin("multiplatform").version(kotlinVersion)
    }
}
rootProject.name = "skiko"
include("ci")
