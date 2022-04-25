pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
    
}
rootProject.name = "SkiaMultiplatformSample"

 //Uncomment, when you want to debug current skiko code
includeBuild("../../skiko") {
    dependencySubstitution {
        substitute(module("org.jetbrains.skiko:skiko")).using(project(":"))
    }
}
