plugins {
    kotlin("multiplatform") version "1.5.21"
}

repositories {
    mavenLocal()
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
                implementation("org.jetbrains.skiko:skiko-js-runtime:$version")
            }
        }
    }
}
