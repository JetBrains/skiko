import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

plugins {
    kotlin("multiplatform") version "1.5.10"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

var version = "0.0.0-SNAPSHOT"
if (project.hasProperty("skiko.version")) {
    version = project.properties["skiko.version"] as String
}

val wasmDistrib by configurations.creating
val wasmDistribDir get() = project.buildDir.resolve("wasm")

val createWasmResources by tasks.registering(Copy::class) {
    from(zipTree(wasmDistrib.first()))
    into(wasmDistribDir)
    include("skiko.js")
    include("skiko.wasm")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()

        // This one is tricky - it has js and wasm binaries required for final linking.
        // We cannot use it directly but need to extract data from there.
        dependencies {
            wasmDistrib("org.jetbrains.skiko:skiko-js-wasm-runtime:$version")
        }
    }

    sourceSets {
        val commonMain by getting {}

        val jsMain by getting {
            dependsOn(commonMain)

            resources.setSrcDirs(resources.srcDirs + createWasmResources.get().destinationDir)

            dependencies {
                implementation("org.jetbrains.skiko:skiko-js-runtime:$version")
            }
        }
    }
}

project.tasks.named("jsProcessResources").get().dependsOn(createWasmResources)

afterEvaluate {
    extensions.configure<NodeJsRootExtension> {
        versions.webpackDevServer.version = "4.0.0"
    }
}

