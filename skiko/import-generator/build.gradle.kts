val kspVersion: String by project

plugins {
    kotlin("multiplatform")
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