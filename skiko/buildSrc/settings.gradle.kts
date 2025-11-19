pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/internal") {
            content {
                includeGroupByRegex("org.jetbrains.compose.internal.*")
            }
        }

        maven("https://maven.pkg.jetbrains.space/public/p/space/maven") {
            content {
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
