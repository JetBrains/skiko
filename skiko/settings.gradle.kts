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
val supportAnyNativeIos = System.getProperty("idea.active")?.toBoolean() == true || listOf(
    "skiko.native.enabled",
    "skiko.native.ios.enabled",
    "skiko.native.ios.arm64.enabled",
    "skiko.native.ios.simulatorArm64.enabled",
    "skiko.native.ios.x64.enabled",
).any { providers.gradleProperty(it).orNull == "true" }
if (supportAnyNativeIos) {
    include("skiko-gpu")
    include("skiko-ganesh-gpu-provider")
    include("skiko-graphite-gpu-provider")
}
include("skiko-graphite")
include("skiko-ganesh")
include("skiko-rendering")
