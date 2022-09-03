buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
        // __KOTLIN_COMPOSE_VERSION__
        classpath(kotlin("gradle-plugin", version = "1.6.10"))
    }
}

plugins {
    kotlin("multiplatform") version "1.6.10"
    id("org.jetbrains.gradle.apple.applePlugin") version "222.3345.143-0.16"
}

val coroutinesVersion = "1.5.2"
val LAUNCH_ON_REAL_DEVICE = false // true, if you want to launch on real device from Xcode

repositories {
    mavenLocal()
    google()
    mavenCentral()
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

val resourcesDir = "$buildDir/resources"
val skikoWasm by configurations.creating

dependencies {
    skikoWasm("org.jetbrains.skiko:skiko-js-wasm-runtime:$version")
}

val unzipTask = tasks.register("unzipWasm", Copy::class) {
    destinationDir = file(resourcesDir)
    from(skikoWasm.map { zipTree(it) })
}

kotlin {
    if (hostOs == "macos") {
        val appleNativeTargets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
        val nativeHostTarget = when (host) {
            "macos-x64" -> macosX64()
            "macos-arm64" -> macosArm64()
            else -> throw GradleException("Host OS is not supported yet")
        }
        appleNativeTargets.add(nativeHostTarget)
        if (LAUNCH_ON_REAL_DEVICE) {
            appleNativeTargets.add(iosArm64())
        }
        val iosTarget = when (host) {
            "macos-x64" -> iosX64()
            "macos-arm64" -> iosSimulatorArm64()
            else -> throw GradleException("Host OS is not supported")
        }
        appleNativeTargets.add(iosTarget)
        appleNativeTargets.forEach {
            // Configure to lauch from Xcode
            it.apply {
                binaries {
                    executable {
                        entryPoint = "org.jetbrains.skiko.sample.main"
                        freeCompilerArgs += listOf(
                            "-linker-option", "-framework", "-linker-option", "Metal",
                            "-linker-option", "-framework", "-linker-option", "CoreText",
                            "-linker-option", "-framework", "-linker-option", "CoreGraphics"
                        )
                    }
                }
            }
        }
        iosTarget.binaries {
            // Configure to launch from AppCode
            framework {
                baseName = "shared"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreText",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics"
                )
            }
        }
    }

    jvm("awt") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

    js(IR) {
        browser()
        binaries.executable()
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

        val awtMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.skiko:skiko-awt-runtime-$hostOs-$hostArch:$version")
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
            resources.setSrcDirs(resources.srcDirs)
            resources.srcDirs(unzipTask.map { it.destinationDir })
        }

        val darwinMain by creating {
            dependsOn(nativeMain)
        }

        val macosMain by creating {
            dependsOn(darwinMain)
        }

        if (hostOs == "macos") {
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
                else -> throw GradleException("Host OS is not supported")
            }
            val iosMain by creating {
                dependsOn(darwinMain)
            }
            val iosSourceSetName = when (host) {
                "macos-x64" -> "iosX64Main"
                "macos-arm64" -> "iosSimulatorArm64Main"
                else -> throw GradleException("Host OS is not supported")
            }
            getByName(iosSourceSetName).dependsOn(iosMain)
            if (LAUNCH_ON_REAL_DEVICE) {
                getByName("iosArm64").dependsOn(iosMain)
            }
        }
    }
}

if (hostOs == "macos") {
    project.tasks.register<Exec>("runIosSim") {
        val device = "iPhone 11"
        workingDir = project.buildDir
        val linkExecutableTaskName = when (host) {
            "macos-x64" -> "linkReleaseExecutableIosX64"
            "macos-arm64" -> "linkReleaseExecutableIosSimulatorArm64"
            else -> throw GradleException("Host OS is not supported")
        }
        val binTask = project.tasks.named(linkExecutableTaskName)
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
    project.tasks.register<Exec>("runNative") {
        workingDir = project.buildDir
        val binTask = project.tasks.named("linkDebugExecutable${hostOs.capitalize()}${hostArch.capitalize()}")
        dependsOn(binTask)
        // Hacky approach.
        commandLine = listOf("bash", "-c")
        argumentProviders.add {
            val out = fileTree(binTask.get().outputs.files.files.single()) { include("*.kexe") }
            println("Run $out")
            listOf(out.single { it.name.endsWith(".kexe") }.absolutePath)
        }
    }
}

project.tasks.register<JavaExec>("runAwt") {
    val kotlinTask =  project.tasks.named("compileKotlinAwt")
    dependsOn(kotlinTask)
    systemProperty("skiko.fps.enabled", "true")
    systemProperty("skiko.linux.autodpi", "true")
    systemProperty("skiko.hardwareInfo.enabled", "true")
    systemProperty("skiko.win.exception.logger.enabled", "true")
    systemProperty("skiko.win.exception.handler.enabled", "true")
    jvmArgs?.add("-ea")
    System.getProperties().entries
        .associate {
            (it.key as String) to (it.value as String)
        }
        .filterKeys { it.startsWith("skiko.") }
        .forEach { systemProperty(it.key, it.value) }
    mainClass.set("org.jetbrains.skiko.sample.App_awtKt")
    classpath(kotlinTask.get().outputs)
    classpath(kotlin.jvm("awt").compilations["main"].runtimeDependencyFiles)
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    dependsOn(unzipTask)
}

enum class Target(val simulator: Boolean, val key: String) {
    WATCHOS_X86(true, "watchos"), WATCHOS_ARM64(false, "watchos"),
    IOS_X64(true, "iosX64"), IOS_ARM64(false, "iosArm64"), IOS_SIMULATOR_ARM64(true, "iosSimulatorArm64")
}


if (hostOs == "macos") {
// Create Xcode integration tasks.
    val sdkName: String? = System.getenv("SDK_NAME")

    val target = sdkName.orEmpty().let {
        when {
            it.startsWith("iphoneos") -> Target.IOS_ARM64
            it.startsWith("watchos") -> Target.WATCHOS_ARM64
            it.startsWith("watchsimulator") -> Target.WATCHOS_X86
            else -> when (host) {
                "macos-x64" -> Target.IOS_X64
                "macos-arm64" -> Target.IOS_SIMULATOR_ARM64
                else -> throw GradleException("Host OS is not supported")
            }
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
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}

apple {
    iosApp {
        productName = "SkikoAppCode"
        sceneDelegateClass = "SceneDelegate"
        dependencies {
            implementation(project(":"))
        }
    }
}
