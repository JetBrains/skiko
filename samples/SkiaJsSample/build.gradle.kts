plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    mavenLocal()
}

var version = "0.0.0-SNAPSHOT"
if (project.hasProperty("skiko.version")) {
    version = project.properties["skiko.version"] as String
}

val resourcesDir = "$buildDir/resources/"

val skikoWasm by configurations.creating

val isCompositeBuild = extra.properties.getOrDefault("skiko.composite.build", "") == "1"

dependencies {
    if (isCompositeBuild) {
        val filePath = gradle.includedBuild("skiko").projectDir
            .resolve("./build/libs/skiko-wasm-$version.jar")
        skikoWasm(files(filePath))
    } else {
        skikoWasm("org.jetbrains.skiko:skiko-js-wasm-runtime:$version")
    }
}

val unzipTask = tasks.register("unzipWasm", Copy::class) {
    destinationDir = file(resourcesDir)
    from(skikoWasm.map { zipTree(it) })

    if (isCompositeBuild) {
        val skikoWasmJarTask = gradle.includedBuild("skiko").task(":skikoWasmJar")
        dependsOn(skikoWasmJarTask)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    dependsOn(unzipTask)
}

kotlin {

    js(IR) {
        moduleName = "jsApp"
        browser {
            commonWebpackConfig {
                outputFileName = "jsApp.js"
            }
        }
        binaries.executable()
    }

    wasmJs() {
        moduleName = "wasmApp"
        browser {
            commonWebpackConfig {
                outputFileName = "wasmApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.skiko:skiko:$version")
            }
        }

        val webMain by creating {
            dependsOn(commonMain)
            resources.setSrcDirs(resources.srcDirs)
            resources.srcDirs(unzipTask.map { it.destinationDir })
        }

        val jsMain by getting {
            dependsOn(webMain)
        }


        val wasmJsMain by getting {
            dependsOn(webMain)
        }
    }
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
   rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}
