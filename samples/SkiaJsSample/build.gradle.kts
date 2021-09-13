plugins {
    kotlin("multiplatform") version "1.5.21"
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

val resourcesDir = "$buildDir/resources/"

kotlin {

    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {}

        val jsMain by getting {
            dependsOn(commonMain)
            dependencies {
                // This one is tricky - it has js and wasm binaries required for final linking.
                // We cannot use it directly but need to extract data from there.
                implementation("org.jetbrains.skiko:skiko-js-runtime:$version")
            }
            resources.setSrcDirs(listOf(resourcesDir))
        }
    }
}

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

