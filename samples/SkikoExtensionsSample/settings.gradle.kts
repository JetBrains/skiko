pluginManagement {
    repositories {
        mavenCentral {
            url = uri("https://cache-redirector.jetbrains.com/maven-central")
        }
        gradlePluginPortal()
        google()
    }

    plugins {
        val kotlinVersion = providers.gradleProperty("kotlin.version").getOrElse("2.3.20")
        kotlin("multiplatform").version(kotlinVersion)
    }
}

rootProject.name = "SkikoExtensionsSample"
