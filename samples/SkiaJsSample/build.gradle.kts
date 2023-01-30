plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
}

var version = "0.0.0-SNAPSHOT"
if (project.hasProperty("skiko.version")) {
    version = project.properties["skiko.version"] as String
}

val resourcesDir = "$buildDir/resources/"

val skikoWasm by configurations.creating

dependencies {
    skikoWasm("org.jetbrains.skiko:skiko-js-wasm-runtime:$version")
}

val unzipTask = tasks.register("unzipWasm", Copy::class) {
    destinationDir = file(resourcesDir)
    from(skikoWasm.map { zipTree(it) })
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    dependsOn(unzipTask)
}

kotlin {

    js(IR) {
        browser()
        binaries.executable()
    }

    wasm() {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.skiko:skiko:$version")
            }
        }

        val jsWasmMain by creating {
            dependsOn(commonMain)
            resources.setSrcDirs(resources.srcDirs)
            resources.srcDirs(unzipTask.map { it.destinationDir })
        }

        val jsMain by getting {
            dependsOn(jsWasmMain)
        }


        val wasmMain by getting {
            dependsOn(jsWasmMain)
        }
    }
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
   rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}

// HACK: some dependencies (coroutines -wasm0 and atomicfu -wasm0) reference deleted *-dev libs
//configurations.all {
//    val conf = this
//    resolutionStrategy.eachDependency {
//        if (requested.version == "1.8.20-dev-3308") {
//            println("Substitute deleted version ${requested.module}:${requested.version} for ${conf.name}")
//            useVersion(project.properties["kotlin.version"] as String)
//        }
//    }
//}
