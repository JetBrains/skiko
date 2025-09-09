import org.jetbrains.kotlin.gradle.plugin.mpp.*

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
        val kotlinVersion = project.property("kotlin.version") as String
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
    }
}

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.gradle.apple.applePlugin") version "222.3345.143-0.16"
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
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

val isCompositeBuild = extra.properties.getOrDefault("skiko.composite.build", "") == "1"
if (project.hasProperty("skiko.version") && isCompositeBuild) {
    project.logger.warn("skiko.version property has no effect when skiko.composite.build is set")
}


val skikoWasm by configurations.creating

dependencies {
    skikoWasm(if (isCompositeBuild) {
        // When we build skiko locally, we have no say in setting skiko.version in the included build.
        // That said, it is always built as "0.0.0-SNAPSHOT" and setting any other version is misleading
        // and can create conflict due to incompatibility of skiko runtime and skiko libs
        files(gradle.includedBuild("skiko").projectDir.resolve("./build/libs/skiko-wasm-0.0.0-SNAPSHOT.jar"))
    } else {
        libs.skiko.wasm.runtime
    })
}

val unpackWasmRuntime = tasks.register("unpackWasmRuntime", Copy::class) {
    destinationDir = file("$buildDir/resources/")
    from(skikoWasm.map { zipTree(it) })

    if (isCompositeBuild) {
        dependsOn(gradle.includedBuild("skiko").task(":skikoWasmJar"))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    dependsOn(unpackWasmRuntime)
}

kotlin {
    if (hostOs == "macos") {
        macosX64() {
            configureToLaunchFromXcode()
        }
        macosArm64() {
            configureToLaunchFromXcode()
        }
        iosSimulatorArm64() {
            configureToLaunchFromAppCode()
            configureToLaunchFromXcode()
        }
        tvosX64() {
            configureToLaunchFromAppCode()
            configureToLaunchFromXcode()
        }
        tvosArm64() {
            configureToLaunchFromAppCode()
            configureToLaunchFromXcode() 
        }
        tvosSimulatorArm64() {
            configureToLaunchFromAppCode()
            configureToLaunchFromXcode()
        }
    }

    jvm("awt") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

    js(IR) {
        moduleName = "clocks-js"
        browser {
            commonWebpackConfig {
                outputFileName = "clocks-js.js"
            }
        }
        binaries.executable()
    }

    wasmJs {
        moduleName = "clocks-wasm"
        browser {
            commonWebpackConfig {
                outputFileName = "clocks-wasm.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.skiko)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val awtMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.skiko.awt.runtime)
            }
        }

        val webMain by creating {
            dependsOn(commonMain)
            resources.setSrcDirs(resources.srcDirs)
            resources.srcDirs(unpackWasmRuntime.map { it.destinationDir })
        }

        val jsMain by getting {
            dependsOn(webMain)
        }

        val wasmJsMain by getting {
            dependsOn(webMain)
        }

        val darwinMain by creating {
            dependsOn(nativeMain)
        }

        val macosMain by creating {
            dependsOn(darwinMain)
        }

        if (hostOs == "macos") {
            val macosX64Main by getting {
                dependsOn(macosMain)
            }
            val macosArm64Main by getting {
                dependsOn(macosMain)
            }
            val uikitMain by creating {
                dependsOn(darwinMain)
            }
            val iosMain by creating {
                dependsOn(uikitMain)
            }
            val iosSimulatorArm64Main by getting {
                dependsOn(iosMain)
            }
            val tvosMain by creating {
                dependsOn(uikitMain)
            }
            val tvosX64Main by getting {
                dependsOn(tvosMain)
            }
            val tvosArm64Main by getting {
                dependsOn(tvosMain)
            }
            val tvosSimulatorArm64Main by getting {
                dependsOn(tvosMain)
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
    val kotlinTask = project.tasks.named("compileKotlinAwt")
    dependsOn(kotlinTask)
    systemProperty("skiko.fps.enabled", "true")
    systemProperty("skiko.linux.autodpi", "true")
    systemProperty("skiko.hardwareInfo.enabled", "true")
    systemProperty("skiko.win.exception.logger.enabled", "true")
    systemProperty("skiko.win.exception.handler.enabled", "true")
    jvmArgs("-ea")
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


enum class Target(val simulator: Boolean, val key: String) {
    WATCHOS_X86(true, "watchos"), 
    WATCHOS_ARM64(false, "watchos"),
    IOS_X64(true, "iosX64"),
    IOS_ARM64(false, "iosArm64"), 
    IOS_SIMULATOR_ARM64(true, "iosSimulatorArm64"),
    TVOS_X64(true, "tvosX64"),
    TVOS_ARM64(true, "tvosArm64"),
    TVOS_SIMULATOR_ARM64(true, "tvosSimulatorArm64"),
}


if (hostOs == "macos") {
// Create Xcode integration tasks.
    val sdkName: String? = System.getenv("SDK_NAME")

    println("Configuring XCode for $sdkName")
    val target = sdkName.orEmpty().let {
        when {
            it.startsWith("iphoneos") -> Target.IOS_ARM64
            it.startsWith("appletvsimulator") -> when (host) {
                "macos-x64" -> Target.TVOS_X64
                "macos-arm64" -> Target.TVOS_SIMULATOR_ARM64
                else -> throw GradleException("Host OS is not supported")
            }
            it.startsWith("appletvos") -> Target.TVOS_ARM64
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
            
            println("Packing for XCode: ${kotlinBinary.target}")

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

fun KotlinNativeTarget.configureToLaunchFromAppCode() {
    binaries {
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

fun KotlinNativeTarget.configureToLaunchFromXcode() {
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


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlinx.cinterop.ExperimentalForeignApi"
    }
}