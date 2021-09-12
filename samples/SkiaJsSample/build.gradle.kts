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
                // implementation("org.jetbrains.skiko:skiko-js-wasm-runtime:$version")
                implementation("org.jetbrains.skiko:skiko-js-runtime:$version")
            }
        }
    }
}
