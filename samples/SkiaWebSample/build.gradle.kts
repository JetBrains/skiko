plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    mavenLocal()
}


val resourcesDir = "$buildDir/resources/"

val isCompositeBuild = extra.properties.getOrDefault("skiko.composite.build", "") == "1"

if (project.hasProperty("skiko.version") && isCompositeBuild) {
    project.logger.warn("skiko.version property has no effect when skiko.composite.build is set")
}

val skikoWasm by configurations.creating

dependencies {
    skikoWasm(if (isCompositeBuild) {
        // When we build skiko locally, we have no say in setting skiko.version in the included build.
        // That said, it is always built as "0.0.0-SNAPSHOT" and setting any other version is misleading
        // and can create conflict due to incompatibility of skiko runtime and skiko libs
        files(gradle.includedBuild("skiko").projectDir.resolve("./build/libs/skiko-wasm-0.0.0-SNAPSHOT.jar"))
    } else {
        libs.skiko.runtime
    })
}

val unpackWasmRuntime = tasks.register("unpackWasmRuntime", Copy::class) {
    destinationDir = file("$buildDir/resources/")
    from(skikoWasm.map { zipTree(it) })

    if (isCompositeBuild) {
        dependsOn(gradle.includedBuild("skiko").task(":skikoWasmJar"))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    dependsOn(unpackWasmRuntime)
}

kotlin {

    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
            }
        }
        binaries.executable()
    }

    wasmJs() {
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.skiko)
            }
        }

        val webMain by creating {
            dependsOn(commonMain)
            resources.setSrcDirs(resources.srcDirs)
            resources.srcDirs(unpackWasmRuntime.map { it.destinationDir })
        }

        val jsMain by getting {
            dependsOn(webMain)
        }

        val wasmJsMain by getting {
            dependsOn(webMain)
        }
    }
}