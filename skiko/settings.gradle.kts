pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    buildscript {
        repositories {
            mavenCentral()
            maven("https://maven.pkg.jetbrains.space/public/p/compose/internal")
            maven("https://maven.pkg.jetbrains.space/public/p/space/maven")
        }
        dependencies {
            // TODO https://youtrack.jetbrains.com/issue/SKIKO-1003/Unify-Maven-publication-of-Skiko-with-Compose
            classpath("org.jetbrains.compose.internal.build-helpers:publishing:0.1.3")
            // used by org.jetbrains.compose.internal.build-helpers:publishing because of https://youtrack.jetbrains.com/issue/CMP-7603/Fix-Maven-Central-publication
            classpath("org.jetbrains:space-sdk-jvm:2024.3-185883")

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
