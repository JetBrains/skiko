include("shared")
include("iosApp")
includeBuild("../../skiko") {
    dependencySubstitution {
        substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
    }
}