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
            classpath("org.jetbrains.compose.internal.build-helpers:publishing:0.1.18")

            classpath("org.kohsuke:github-api:1.329")

            // Added dependency for Android Gradle plugin
            classpath("com.android.tools.build:gradle:7.4.2")
        }
    }

    plugins {
        // whatever kotlinVersion we point to here, it will be ignored due to the nature of gradle design
        // the actual version is set in buildSrc/gradle.properties
        kotlin("jvm")
        kotlin("multiplatform")
        id("com.android.library").version("7.4.2") apply false
    }
}
rootProject.name = "skiko"
include("ci")
include("import-generator")
