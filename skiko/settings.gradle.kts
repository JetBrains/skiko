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
