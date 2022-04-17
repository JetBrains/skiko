plugins {
    kotlin("multiplatform") version "1.6.20"
}

group = "me.user"
version = "1.0-SNAPSHOT"

repositories {
    google()
    jcenter()
}

val versionSkiko = "0.0.0-SNAPSHOT"
val coroutinesVersion = "1.5.2"

kotlin {
    ios {
        binaries {
            framework {
                baseName = "shared"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreText",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics"
                )
            }
//            executable {
//                entryPoint = "me.user.shared.main"
//                freeCompilerArgs += listOf(
//                    "-linker-option", "-framework", "-linker-option", "Metal",
//                    "-linker-option", "-framework", "-linker-option", "CoreText",
//                    "-linker-option", "-framework", "-linker-option", "CoreGraphics"
//                )
//            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.skiko:skiko:$versionSkiko")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val iosMain by getting
        val iosTest by getting
    }
}

