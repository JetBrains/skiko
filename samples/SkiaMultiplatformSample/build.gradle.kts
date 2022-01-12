buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.1.0-beta05")
        // __KOTLIN_COMPOSE_VERSION__
        classpath(kotlin("gradle-plugin", version = "1.6.10"))
    }
}

plugins {
    kotlin("multiplatform") version "1.6.10"
    id("com.android.application") version "7.1.0-beta05"
}

val coroutinesVersion = "1.5.2"

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
    val targets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()

    if (hostOs == "macos") {
        val nativeHostTarget = when (host) {
            "macos-x64" -> macosX64()
            "macos-arm64" -> macosArm64()
            else -> throw GradleException("Host OS is not supported yet")
        }
        targets.add(nativeHostTarget)

        targets.add(iosX64())
        targets.add(iosArm64())
    }

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

    js(IR) {
        browser()
        binaries.executable()
    }

    targets.forEach {
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

        val jvmMain by getting {
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

        val androidMain by creating {
            dependsOn(commonMain)
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
            val iosX64Main by getting {
                dependsOn(iosMain)
            }
            val iosArm64Main by getting {
                dependsOn(iosMain)
            }
        }
    }
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "org.gradle.samples"
        minSdkVersion(16)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

project.tasks.register<JavaExec>("runJvm") {
    val kotlinTask =  project.tasks.named("compileKotlinJvm")
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
    mainClass.set("org.jetbrains.skiko.sample.App_jvmKt")
    classpath(kotlinTask.get().outputs)
    classpath(kotlin.jvm().compilations["main"].runtimeDependencyFiles)
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    dependsOn(unzipTask)
}

enum class Target(val simulator: Boolean, val key: String) {
    WATCHOS_X86(true, "watchos"), WATCHOS_ARM64(false, "watchos"),
    IOS_X64(true, "iosX64"), IOS_ARM64(false, "iosArm64")
}


if (hostOs == "macos") {
// Create Xcode integration tasks.
    val sdkName: String? = System.getenv("SDK_NAME")

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
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}
