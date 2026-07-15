dependencyResolutionManagement {
    versionCatalogs {
        register("libs") {
            from(files("../dependencies.toml"))
        }
    }
}

pluginManagement {
    repositories {
        mavenCentral {
            url = uri("https://cache-redirector.jetbrains.com/maven-central")
        }
        gradlePluginPortal()
        google()
    }
}
rootProject.name = "skiko"
include("ci")
include("docs")
include("import-generator")
include("test-utils")
include("skiko-skottie")
