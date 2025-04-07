pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
    buildscript {
        repositories {
            mavenCentral()
            maven("https://maven.pkg.jetbrains.space/public/p/compose/internal")
            maven("https://maven.pkg.jetbrains.space/public/p/space/maven")
            google()
        }
        dependencies {
            // TODO Removing this makes publishing module below crash android internal plugin
            classpath("org.ow2.asm:asm:9.6")

            // TODO https://youtrack.jetbrains.com/issue/SKIKO-1003/Unify-Maven-publication-of-Skiko-with-Compose
            classpath("org.jetbrains.compose.internal.build-helpers:publishing:0.1.3")
            // used by org.jetbrains.compose.internal.build-helpers:publishing because of https://youtrack.jetbrains.com/issue/CMP-7603/Fix-Maven-Central-publication
            classpath("org.jetbrains:space-sdk-jvm:2024.3-185883")

            classpath("org.kohsuke:github-api:1.116")

            // Added dependency for Android Gradle plugin
            classpath("com.android.tools.build:gradle:8.2.2")
        }
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        kotlin("jvm").version(kotlinVersion)
        kotlin("multiplatform").version(kotlinVersion)
        id("com.android.library").version("8.2.2") apply false
    }
}
rootProject.name = "skiko"
include("ci")
include("import-generator")
