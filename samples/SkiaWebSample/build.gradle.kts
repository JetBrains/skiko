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

// When we build skiko locally we have no say in setting skiko.version in the included build.
// That said, it is always built as "0.0.0-SNAPSHOT" and setting any other version not only is misleading,
// but even can create conflict due to incompatibility of skiko runtime and skiko libs
val skikoVersion = if (isCompositeBuild) "0.0.0-SNAPSHOT" else project.properties["skiko.version"]

val skikoWasm by configurations.creating
dependencies {
    skikoWasm(
        if (isCompositeBuild) files(
            gradle.includedBuild("skiko")
                .projectDir
                .resolve("./build/libs/skiko-wasm-$skikoVersion.jar")
        ) else libs.skiko.runtime
    )
}

val unzipTask = tasks.register("unzipWasm", Copy::class) {
    destinationDir = file(resourcesDir)
    from(skikoWasm.map { zipTree(it) })

    if (isCompositeBuild) {
        // we only can access the "skikoWasmJar" task from includedBuild as TaskReference
        // so we don't have access to its output and have to copy it as a hardcoded path in dependencies
        dependsOn(
            gradle.includedBuild("skiko").task(":skikoWasmJar")
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    dependsOn(unzipTask)
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

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
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
            resources.srcDirs(unzipTask.map { it.destinationDir })
        }

        val jsMain by getting {
            dependsOn(webMain)
        }


        val wasmJsMain by getting {
            dependsOn(webMain)

            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3")
            }
        }
    }
}

