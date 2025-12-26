@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import java.util.Locale

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

val defDir = projectDir.resolve("src/nativeInterop/cinterop").apply { mkdirs() }
val libDestination = layout.buildDirectory.dir("interopLib").get().apply {
    asFile.mkdirs()
}
val tmpBuildDir = layout.buildDirectory.dir("tmp").get().apply {
    asFile.mkdirs()
}

// Provide a real storage for __libc_single_threaded to avoid ld --defsym hack causing segfaults
val shimDir = layout.buildDirectory.dir("shim").get().asFile
val buildShim by tasks.registering(Exec::class) {
    doFirst {
        if (!shimDir.exists()) shimDir.mkdirs()
        val cFile = shimDir.resolve("libc_single_threaded_shim.c")
        cFile.writeText(
            """
            // Minimal shim to provide __libc_single_threaded variable expected by
            // libstdc++/Skia builds on some Linux distributions.
            // Defining real storage prevents the dynamic loader from writing to address 0.
            __attribute__((visibility("default"))) int __libc_single_threaded = 1;
            """.trimIndent()
        )
    }
    workingDir = shimDir
    // Compile and archive into a static library
    commandLine = listOf("bash", "-lc", "gcc -c -fPIC libc_single_threaded_shim.c -o libc_single_threaded_shim.o && ar rcs libshim.a libc_single_threaded_shim.o")
    outputs.files(shimDir.resolve("libshim.a"))
}

val kotlinNativeDataPath = System.getenv("KONAN_DATA_DIR")?.let { File(it) }
    ?: File(System.getProperty("user.home")).resolve(".konan")

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
    destinationDir = layout.buildDirectory.dir("resources").get().asFile
    from(skikoWasm.map { zipTree(it) })

    if (isCompositeBuild) {
        dependsOn(gradle.includedBuild("skiko").task(":skikoWasmJar"))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    dependsOn(unpackWasmRuntime)
}

kotlin {
    if (hostOs == "linux") {
        linuxX64 {
            binaries.executable {
                entryPoint = "org.jetbrains.skiko.sample.main"
                // Ensure shim is built before linking
                project.tasks.named(linkTaskName).configure { dependsOn(buildShim) }
                linkerOpts(
                    // System library paths
                    "-L/usr/lib",
                    "-L/usr/lib64",

                    // Link shim providing __libc_single_threaded storage to satisfy libstdc++/Skia
                    "-L${shimDir.absolutePath}",
                    "-lshim",

                    // Libraries required by RGFW/KGFW and Skia
                    "-Wl,--allow-shlib-undefined",
                    "-lX11",
                    "-lXext",
                    "-lXrandr",
                    "-lXcursor",
                    "-lXi",
                    "-lXinerama",
                    "-lGL",
                    "-lstdc++",
                    "-lfontconfig",
                    "-lfreetype",
                    "-lpthread",
                    "-lc",
                    // Avoid linking system harfbuzz/icu to prevent version mismatches; Skia pack provides required deps
                    // "-lharfbuzz",
                    // "-licui18n",
                    "-ldl"
                )
            }
        }
    }

    if (hostOs == "macos") {
        macosX64 {
            configureToLaunchFromXcode()
        }
        macosArm64 {
            configureToLaunchFromXcode()
        }
        iosSimulatorArm64 {
            configureToLaunchFromAppCode()
            configureToLaunchFromXcode()
        }
        tvosX64 {
            configureToLaunchFromAppCode()
            configureToLaunchFromXcode()
        }
        tvosArm64 {
            configureToLaunchFromAppCode()
            configureToLaunchFromXcode() 
        }
        tvosSimulatorArm64 {
            configureToLaunchFromAppCode()
            configureToLaunchFromXcode()
        }
    }

    jvm("awt") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
            }
        }
        binaries.executable()
    }

    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
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
            dependencies {
                implementation("io.github.drulysses:kgfw:1.2.0")
            }
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

        val linuxMain by creating {
            dependsOn(nativeMain)
        }

        val darwinMain by creating {
            dependsOn(nativeMain)
        }

        val macosMain by creating {
            dependsOn(darwinMain)
        }

        if (hostOs == "linux") {
            val linuxX64Main by getting {
                dependsOn(linuxMain)
            }
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

    compilerOptions.optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
}

if (hostOs == "macos") {
    project.tasks.register<Exec>("runIosSim") {
        val device = "iPhone 11"
        workingDir = layout.buildDirectory.get().asFile
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
        NativeBuildType.valueOf(it.uppercase(Locale.getDefault()))
    } ?: NativeBuildType.DEBUG

    val currentTarget = kotlin.targets[target.key] as KotlinNativeTarget
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
            dependsOn(kotlinBinary.linkTaskProvider)
            
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
