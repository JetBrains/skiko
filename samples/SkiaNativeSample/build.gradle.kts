plugins {
    kotlin("multiplatform") version "1.5.31"
}

repositories {
    mavenLocal()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val osName = System.getProperty("os.name")
val targetOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
var targetArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val target = "${targetOs}-${targetArch}"

var version = "0.0.0-SNAPSHOT"
if (project.hasProperty("skiko.version")) {
    version = project.properties["skiko.version"] as String
}

kotlin {
    val nativeTarget = when (target) {
        "macos-x64" -> macosX64()
        "macos-arm64" -> macosArm64()
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "org.jetbrains.skiko.sample.native.main"
                freeCompilerArgs += listOf("-linker-options", "-framework", "-linker-option", "Metal")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.skiko:skiko:$version")
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val archTargetMain = when (target) {
            "macos-x64" -> {
                val macosX64Main by getting {
                    dependsOn(nativeMain)
                    dependencies {
                        // TODO: can we do better?
                        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-macosx64:1.5.2")
                    }
                }
                macosX64Main
            }
            "macos-arm64" -> {
                val macosArm64Main by getting {
                    dependsOn(nativeMain)
                    dependencies {
                        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-macosarm64:1.5.2")
                    }
                }
                macosArm64Main
            }
            else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
        }
    }
}
