@file:OptIn(ExperimentalWasmDsl::class)

import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget

buildscript {
    repositories {
        google()
        mavenCentral {
            url = uri("https://cache-redirector.jetbrains.com/maven-central")
        }
        maven("https://redirector.kotlinlang.org/maven/compose-dev")
    }

    dependencies {
        val kotlinVersion = project.property("kotlin.version") as String
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
    }
}

plugins {
    kotlin("multiplatform")
}

repositories {
    google()
    mavenCentral {
        url = uri("https://cache-redirector.jetbrains.com/maven-central")
    }
    mavenLocal()
    maven("https://redirector.kotlinlang.org/maven/compose-dev")
}

val osName = System.getProperty("os.name")
val hostOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
val hostArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val host = "${hostOs}-${hostArch}"
val skikoVersion = findProperty("skiko.version")?.toString() ?: "0.0.0-SNAPSHOT"

kotlin {
    if (hostOs == "macos") {
        macosX64() {
            configureToLaunchFromXcode()
        }
        macosArm64() {
            configureToLaunchFromXcode()
        }
        iosArm64() {
            configureToLaunchFromAppCode()
            configureToLaunchFromXcode()
        }
        iosSimulatorArm64() {
            configureToLaunchFromAppCode()
            configureToLaunchFromXcode()
        }
        iosX64() {
            configureToLaunchFromAppCode()
            configureToLaunchFromXcode()
        }
    }

    jvm("awt") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
                }
            }
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            }
        }

        val awtMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.skiko:skiko:$skikoVersion")
                implementation("org.jetbrains.skiko:skiko-skottie:$skikoVersion")
                implementation("org.jetbrains.skiko:skiko-awt-runtime-all:$skikoVersion")
                implementation("org.jetbrains.skiko:skiko-skottie-awt-runtime-all:$skikoVersion")
            }
        }

        val webMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
            }
        }

        val jsMain by getting {
            dependsOn(webMain)
            dependencies {
                implementation("org.jetbrains.skiko:skiko-js:$skikoVersion")
                implementation("org.jetbrains.skiko:skiko-skottie-js:$skikoVersion")
            }
        }

        val wasmJsMain by getting {
            dependsOn(webMain)
            dependencies {
                implementation("org.jetbrains.skiko:skiko-wasm-js:$skikoVersion")
                implementation("org.jetbrains.skiko:skiko-skottie-wasm-js:$skikoVersion")
            }
        }

        if (hostOs == "macos") {
            val nativeMain by creating {
                dependsOn(commonMain)
                dependencies {
                    implementation("org.jetbrains.skiko:skiko:$skikoVersion")
                    implementation("org.jetbrains.skiko:skiko-skottie:$skikoVersion")
                }
            }
            val darwinMain by creating {
                dependsOn(nativeMain)
            }
            val macosMain by creating {
                dependsOn(darwinMain)
            }
            val macosX64Main by getting {
                dependsOn(macosMain)
            }
            val macosArm64Main by getting {
                dependsOn(macosMain)
            }
            val iosMain by creating {
                dependsOn(darwinMain)
            }
            val iosArm64Main by getting {
                dependsOn(iosMain)
            }
            val iosSimulatorArm64Main by getting {
                dependsOn(iosMain)
            }
            val iosX64Main by getting {
                dependsOn(iosMain)
            }
        }
    }
    targets.withType<KotlinJsIrTarget>().all { configureSkikoWebRuntime(project, this) }
}

if (hostOs == "macos") {
    val iosSimDevice = providers.gradleProperty("skiko.iosSimulatorDevice").orElse("booted")
    val iosSimAppName = "SkikoExtensionsSample"
    val iosSimBundleId = "org.jetbrains.skiko.sample.extensions"
    val iosSimLinkExecutableTaskName = when (host) {
        "macos-x64" -> throw GradleException("runIosSim is supported only on Apple Silicon hosts (iosSimulatorArm64 target)")
        "macos-arm64" -> "linkReleaseExecutableIosSimulatorArm64"
        else -> throw GradleException("Host OS is not supported")
    }

    val packageIosSimApp = project.tasks.register("packageIosSimApp") {
        val binTask = project.tasks.named(iosSimLinkExecutableTaskName)
        dependsOn(binTask)
        doLast {
            val executable = fileTree(binTask.get().outputs.files.files.single()) { include("*.kexe") }
                .single { it.name.endsWith(".kexe") }

            val appDir = project.layout.buildDirectory.dir("iosSimulator/${iosSimAppName}.app").get().asFile
            appDir.mkdirs()

            val targetExecutable = appDir.resolve(iosSimAppName)
            executable.copyTo(targetExecutable, overwrite = true)
            targetExecutable.setExecutable(true)

            appDir.resolve("PkgInfo").writeText("APPL????")
            val plistTemplate = project.file("plists/Ios/Info.plist").readText()
            appDir.resolve("Info.plist").writeText(
                plistTemplate
                    .replace("$(DEVELOPMENT_LANGUAGE)", "en")
                    .replace("$(EXECUTABLE_NAME)", iosSimAppName)
                    .replace("$(PRODUCT_BUNDLE_IDENTIFIER)", iosSimBundleId)
                    .replace("$(PRODUCT_NAME)", iosSimAppName)
            )
        }
    }

    project.tasks.register("runIosSim") {
        dependsOn(packageIosSimApp)
        doLast {
            fun runCommand(command: List<String>, ignoreFailure: Boolean = false) {
                val process = ProcessBuilder(command)
                    .directory(project.projectDir)
                    .inheritIO()
                    .start()
                val exitCode = process.waitFor()
                if (exitCode != 0 && !ignoreFailure) {
                    throw GradleException("Command failed ($exitCode): ${command.joinToString(" ")}")
                }
            }

            val appDir =
                project.layout.buildDirectory.dir("iosSimulator/${iosSimAppName}.app").get().asFile.absolutePath
            val device = iosSimDevice.get()
            val launchTarget = if (device == "booted") "booted" else device

            if (device != "booted") {
                runCommand(listOf("xcrun", "simctl", "boot", device), ignoreFailure = true)
                runCommand(listOf("xcrun", "simctl", "bootstatus", device, "-b"))
            }

            runCommand(listOf("xcrun", "simctl", "install", launchTarget, appDir))
            runCommand(listOf("xcrun", "simctl", "launch", "--terminate-running-process", launchTarget, iosSimBundleId))
        }
    }

    project.tasks.register<Exec>("runNative") {
        workingDir = project.buildDir
        val hostOsCap = hostOs.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val hostArchCap = hostArch.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val binTask = project.tasks.named("linkDebugExecutable${hostOsCap}${hostArchCap}")
        dependsOn(binTask)
        // Hacky approach.
        commandLine = listOf("bash", "-c")
        argumentProviders.add {
            val out = fileTree(binTask.get().outputs.files.files.single()) { include("*.kexe") }
            println("Run $out")
            val executable = out.single { it.name.endsWith(".kexe") }.absolutePath
            val exitArg = providers.gradleProperty("sample.exitAfterMs").orNull?.let { "--exit-after-ms=$it" }
            listOf(listOfNotNull(executable, exitArg).joinToString(" ") { "'${it.replace("'", "'\\''")}'" })
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
    mainClass.set("org.jetbrains.skiko.sample.extensions.App_awtKt")
    classpath(kotlinTask.get().outputs)
    classpath(kotlin.jvm("awt").compilations["main"].runtimeDependencyFiles)
}

enum class Target(val simulator: Boolean, val key: String) {
    IOS_X64(true, "iosX64"),
    IOS_ARM64(false, "iosArm64"),
    IOS_SIMULATOR_ARM64(true, "iosSimulatorArm64"),
}

if (hostOs == "macos") {
// Create Xcode integration tasks.
    val sdkName: String? = System.getenv("SDK_NAME")

    println("Configuring XCode for $sdkName")
    val target = sdkName.orEmpty().let {
        when {
            it.startsWith("iphoneos") -> Target.IOS_ARM64
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
        org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.valueOf(it.uppercase())
    } ?: org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG

    val currentTarget = kotlin.targets[target.key] as org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
    val kotlinBinary = currentTarget.binaries.getExecutable(buildType)
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
            entryPoint = "org.jetbrains.skiko.sample.extensions.main"
            freeCompilerArgs += listOf(
                "-linker-option", "-framework", "-linker-option", "Metal",
                "-linker-option", "-framework", "-linker-option", "CoreText",
                "-linker-option", "-framework", "-linker-option", "CoreGraphics"
            )
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
}

private fun configureSkikoWebRuntime(
    project: Project,
    target: KotlinJsIrTarget,
) {
    val titledTargetName = target.name.replaceFirstChar { it.titlecase() }
    val mainCompilation = target.compilations.findByName(KotlinCompilation.MAIN_COMPILATION_NAME)!!
    val runtimeDepsConfig = project.configurations.findByName(mainCompilation.runtimeDependencyConfigurationName)!!
    val skikoWebRuntimeJarFiles = runtimeDepsConfig.incoming.artifactView {
        @Suppress("UnstableApiUsage")
        withVariantReselection()
        attributes {
            runtimeDepsConfig.attributes.keySet().forEach {
                @Suppress("UNCHECKED_CAST")
                attribute(it as Attribute<Any>, runtimeDepsConfig.attributes.getAttribute(it) as Any)
            }
            attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, "skiko-runtime"))
        }
    }.files
    val unpackedRuntimeDir = project.layout.buildDirectory.dir("compose/skiko-${target.name}-runtime")

    val unpackRuntime = project.tasks.register("unpackSkikoRuntimeFor$titledTargetName", Copy::class.java) {
        destinationDir = unpackedRuntimeDir.get().asFile
        from(
            skikoWebRuntimeJarFiles.map { artifact -> project.zipTree(artifact) }
        )
        exclude("META-INF/**")
    }

    target.compilations.all {
        if (target.wasmTargetType != null) {
            // Kotlin/Wasm uses ES module system to depend on skiko through skiko.mjs.
            // Further bundler could process all files by its own (both skiko.mjs and skiko.wasm) and then emits its own version.
            // So that’s why we need to provide skiko.mjs and skiko.wasm only for webpack, but not in the final dist.
            binaries.all {
                linkSyncTask.configure {
                    dependsOn(unpackRuntime)
                    from.from(unpackedRuntimeDir)
                }
            }
        } else {
            // Kotlin/JS depends on Skiko through global space.
            // Bundler cannot know anything about global externals, so that’s why we need to copy it to final dist
            project.tasks.named(processResourcesTaskName, ProcessResources::class.java) {
                from(unpackedRuntimeDir)
                dependsOn(unpackRuntime)
                exclude("META-INF")
            }
        }
    }
}
