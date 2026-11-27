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
val supportGpuProviders = System.getProperty("idea.active")?.toBoolean() == true || listOf(
    "skiko.native.enabled",
    "skiko.native.mac.enabled",
    "skiko.native.ios.enabled",
    "skiko.native.ios.arm64.enabled",
    "skiko.native.ios.simulatorArm64.enabled",
    "skiko.native.ios.x64.enabled",
    "skiko.native.tvos.enabled",
    "skiko.native.tvos.arm64.enabled",
    "skiko.native.tvos.simulatorArm64.enabled",
    "skiko.native.tvos.x64.enabled",
).any { providers.gradleProperty(it).orNull == "true" }
// Graphite provider has no tvOS targets, so it is not needed (and would have no targets) for tvOS-only builds.
val supportGraphiteGpuProvider = System.getProperty("idea.active")?.toBoolean() == true || listOf(
    "skiko.native.enabled",
    "skiko.native.mac.enabled",
    "skiko.native.ios.enabled",
    "skiko.native.ios.arm64.enabled",
    "skiko.native.ios.simulatorArm64.enabled",
    "skiko.native.ios.x64.enabled",
).any { providers.gradleProperty(it).orNull == "true" }
if (supportGpuProviders) {
    include("skiko-gpu")
    include("skiko-ganesh-gpu-provider")
}
if (supportGraphiteGpuProvider) {
    include("skiko-graphite-gpu-provider")
}
include("skiko-graphite")
include("runtime-publications")
include("skiko-platform")
include("skiko-ganesh")
