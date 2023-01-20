pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
        }
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        kotlin("multiplatform").version(kotlinVersion)
    }

}
rootProject.name = "SkiaJsSample"

