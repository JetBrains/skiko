pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
    buildscript {
        repositories {
            mavenLocal()
            mavenCentral()
            maven("https://maven.pkg.jetbrains.space/public/p/compose/internal")
            maven("https://maven.pkg.jetbrains.space/public/p/space/maven")
            maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
            maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
            maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
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
include("import-generator")
