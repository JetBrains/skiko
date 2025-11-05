dependencyResolutionManagement {
    versionCatalogs {
        register("libs") {
            from(files("../dependencies.toml"))
        }
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}
rootProject.name = "skiko"
include("ci")
include("import-generator")
