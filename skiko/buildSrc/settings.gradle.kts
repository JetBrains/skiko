pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://packages.jetbrains.team/maven/p/cmp/dev") {
            content {
                includeGroupByRegex("org.jetbrains.compose.internal.*")
                includeModuleByRegex("org.jetbrains", ".*space.*")
            }
        }

        google {
            mavenContent {
                includeGroupByRegex(".*android.*")
                includeGroupByRegex(".*google.*")
            }
        }

        gradlePluginPortal {
            content {
                includeGroupByRegex(".*com\\.gradle.*")
                includeGroupByRegex(".*org\\.gradle.*")
                includeModule("org.jetbrains.kotlinx", "kotlinx-benchmark-plugin")
            }
        }

        mavenCentral()
    }

    versionCatalogs {
        register("libs") {
            from(files("../../dependencies.toml"))
        }
    }
}
