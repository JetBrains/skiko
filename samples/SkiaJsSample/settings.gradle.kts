pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
        }
    }
    
}
rootProject.name = "SkiaJsSample"

if (System.getenv("SKIKO_COMPOSITE_BUILD") == "1" || System.getProperty("skikoCompositeBuild") == "true") {
    includeBuild("../../skiko") {
        dependencySubstitution {
            substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
        }
    }
}
