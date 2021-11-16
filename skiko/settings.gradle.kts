pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    buildscript {
        repositories {
            mavenCentral()
            maven("https://maven.pkg.jetbrains.space/public/p/compose/internal")
            maven("https://maven.pkg.jetbrains.space/public/p/space/maven")
        }
        dependencies {
            classpath("org.jetbrains.compose.internal.build-helpers:publishing:0.1.0")
            classpath("org.kohsuke:github-api:1.116")
        }
    }
}
rootProject.name = "skiko"
include("ci")