plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral {
        url = uri("https://cache-redirector.jetbrains.com/maven-central")
    }
    maven("https://redirector.kotlinlang.org/maven/compose-dev")
    mavenLocal()
}

val isCompositeBuild = extra.properties.getOrDefault("skiko.composite.build", "0") == "1" ||
        runCatching { gradle.includedBuild("skiko") }.isSuccess ||
        (gradle.parent?.includedBuilds?.any { it.name == "skiko" } ?: false)

fun getSkikoIncludedBuild() = runCatching { gradle.includedBuild("skiko") }.getOrNull()
    ?: gradle.parent?.includedBuild("skiko")
    ?: error("skiko included build not found")

if (project.hasProperty("skiko.version") && isCompositeBuild) {
    project.logger.warn("skiko.version property has no effect when skiko.composite.build is set")
}

val skikoWasm by configurations.creating

dependencies {
    skikoWasm(if (isCompositeBuild) {
        // When we build skiko locally, we have no say in setting skiko.version in the included build.
        // That said, it is always built as "0.0.0-SNAPSHOT" and setting any other version is misleading
        // and can create conflict due to incompatibility of skiko runtime and skiko libs
        files(getSkikoIncludedBuild().projectDir.resolve("./build/libs/skiko-wasm-0.0.0-SNAPSHOT.jar"))
    } else {
        libs.skiko.wasm.runtime
    })
}

val unpackWasmRuntime = tasks.register("unpackWasmRuntime", Copy::class) {
    destinationDir = file("$buildDir/resources/")
    from(skikoWasm.map { zipTree(it) })

    if (isCompositeBuild) {
        dependsOn(getSkikoIncludedBuild().task(":skikoWasmJar"))
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

    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.skiko)
        }

        webMain {
            dependencies {
                implementation(libs.browser)
            }

            resources.srcDirs(unpackWasmRuntime.map { it.destinationDir })
        }
    }
}