val kspVersion: String by project

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral {
        url = uri("https://cache-redirector.jetbrains.com/maven-central")
    }
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                compileOnly(kotlin("compiler-embeddable"))
            }
        }
    }
}