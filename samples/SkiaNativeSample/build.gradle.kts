plugins {
    kotlin("multiplatform") version "1.5.31"
}

val coroutinesVersion = "1.5.2"

repositories {
    mavenLocal()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val osName = System.getProperty("os.name")
val hostOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
var hostArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val host = "${hostOs}-${hostArch}"

var version = "0.0.0-SNAPSHOT"
if (project.hasProperty("skiko.version")) {
    version = project.properties["skiko.version"] as String
}

kotlin {
    val targets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
    val nativeHostTarget = when (host) {
        "macos-x64" -> macosX64()
        "macos-arm64" -> macosArm64()
        else -> throw GradleException("Host OS is not supported yet")
    }

    targets.add(nativeHostTarget)

    if (hostOs == "macos") {
        targets.add(iosX64())
        targets.add(iosArm64())
    }

    targets.forEach {
        it.apply {
            binaries {
                executable {
                    entryPoint = "org.jetbrains.skiko.sample.main"
                    freeCompilerArgs += listOf(
                        "-linker-option", "-framework", "-linker-option", "Metal",
                        "-linker-option", "-framework", "-linker-option", "CoreText",
                    )
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.skiko:skiko:$version")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val macosMain by creating {
            dependsOn(nativeMain)
        }

        val archTargetMain = when (host) {
            "macos-x64" -> {
                val macosX64Main by getting {
                    dependsOn(macosMain)
                }
                macosX64Main
            }
            "macos-arm64" -> {
                val macosArm64Main by getting {
                    dependsOn(macosMain)
                }
                macosArm64Main
            }
            else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
        }

        if (hostOs == "macos") {
            val iosMain by creating {
                dependsOn(nativeMain)
            }
            val iosX64Main by getting {
                dependsOn(iosMain)
            }
            val iosArm64Main by getting {
                dependsOn(iosMain)
            }
        }
    }
}

project.tasks.register<Exec>("runIosSim") {
    val device = "iPhone 11"
    workingDir = project.buildDir
    val binTask = project.tasks.named("linkReleaseExecutableIosX64")
    dependsOn(binTask)
    commandLine = listOf(
        "xcrun",
        "simctl",
        "spawn",
        "--standalone",
        device
    )
    argumentProviders.add {
        val out = fileTree(binTask.get().outputs.files.files.single()) { include("*.kexe") }
        listOf(out.single { it.name.endsWith(".kexe") }.absolutePath)
    }
}

// Create Xcode integration tasks.
val sdkName: String? = System.getenv("SDK_NAME")

enum class Target(val simulator: Boolean, val key: String) {
    WATCHOS_X86(true, "watchos"), WATCHOS_ARM64(false, "watchos"),
    IOS_X64(true, "iosX64"), IOS_ARM64(false, "iosArm64")
}

val target = sdkName.orEmpty().let {
    when {
        it.startsWith("iphoneos") -> Target.IOS_ARM64
        it.startsWith("iphonesimulator") -> Target.IOS_X64
        it.startsWith("watchos") -> Target.WATCHOS_ARM64
        it.startsWith("watchsimulator") -> Target.WATCHOS_X86
        else -> Target.IOS_X64
    }
}

val targetBuildDir: String? = System.getenv("TARGET_BUILD_DIR")
val executablePath: String? = System.getenv("EXECUTABLE_PATH")
val buildType = System.getenv("CONFIGURATION")?.let {
    org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.valueOf(it.toUpperCase())
} ?: org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG

val currentTarget = kotlin.targets[target.key] as org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
val kotlinBinary = currentTarget.binaries.getExecutable(buildType)
val xcodeIntegrationGroup = "Xcode integration"

val packForXCode = if (sdkName == null || targetBuildDir == null || executablePath == null) {
    // The build is launched not by Xcode ->
    // We cannot create a copy task and just show a meaningful error message.
    tasks.create("packForXCode").doLast {
        throw IllegalStateException("Please run the task from Xcode")
    }
} else {
    // Otherwise copy the executable into the Xcode output directory.
    tasks.create("packForXCode", Copy::class.java) {
        dependsOn(kotlinBinary.linkTask)

        destinationDir = file(targetBuildDir)

        val dsymSource = kotlinBinary.outputFile.absolutePath + ".dSYM"
        val dsymDestination = File(executablePath).parentFile.name + ".dSYM"
        val oldExecName = kotlinBinary.outputFile.name
        val newExecName = File(executablePath).name

        from(dsymSource) {
            into(dsymDestination)
            rename(oldExecName, newExecName)
        }

        from(kotlinBinary.outputFile) {
            rename { executablePath }
        }
    }
}
