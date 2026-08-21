@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.gradle.api.attributes.Usage
import org.gradle.nativeplatform.MachineArchitecture
import org.gradle.nativeplatform.OperatingSystemFamily
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral {
        url = uri("https://cache-redirector.jetbrains.com/maven-central")
    }
    google()
    mavenLocal()
    maven("https://redirector.kotlinlang.org/maven/compose-dev")
}

val isExplicitCompositeBuild = extra.properties.getOrDefault("skiko.composite.build", "") == "1"
val skikoVersion = providers.gradleProperty("skiko.version")
val skikoVersionOrUnspecified = skikoVersion.orElse("unspecified")
val isCompositeBuild = isExplicitCompositeBuild || !skikoVersion.isPresent
val runArguments: String? by project

val hostOs = when {
    System.getProperty("os.name") == "Mac OS X" -> "macos"
    System.getProperty("os.name").startsWith("Win") -> "windows"
    System.getProperty("os.name").startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: ${System.getProperty("os.name")}")
}

val hostArch = when (System.getProperty("os.arch")) {
    "x86_64", "amd64" -> "x64"
    "aarch64", "arm64" -> "arm64"
    else -> error("Unsupported arch: ${System.getProperty("os.arch")}")
}

val hostAwtRuntimeArtifact = "org.jetbrains.skiko:skiko-awt-runtime-$hostOs-$hostArch:${skikoVersionOrUnspecified.get()}"
val localAwtRuntimeJar = if (isCompositeBuild) {
    gradle.includedBuild("skiko").projectDir.resolve("build/libs/skiko-runtime-for-tests.jar")
} else {
    null
}

val hostOperatingSystemFamily = when (hostOs) {
    "macos" -> OperatingSystemFamily.MACOS
    "windows" -> OperatingSystemFamily.WINDOWS
    "linux" -> OperatingSystemFamily.LINUX
    else -> error("Unsupported OS: $hostOs")
}

val hostMachineArchitecture = when (hostArch) {
    "x64" -> MachineArchitecture.X86_64
    "arm64" -> MachineArchitecture.ARM64
    else -> error("Unsupported arch: $hostArch")
}

if (project.hasProperty("skiko.version") && isExplicitCompositeBuild) {
    project.logger.warn("skiko.version property has no effect when skiko.composite.build is set")
}

fun requireSkikoVersionForPublishedArtifacts() {
    if (!isExplicitCompositeBuild && !skikoVersion.isPresent) {
        throw GradleException(
            "Missing required Gradle property: skiko.version. " +
                "Pass -Pskiko.version=<skiko-version> or use run_benchmarks.main.kts version=<skiko-version>."
        )
    }
}

if (gradle.startParameter.taskNames.any { it.referencesSkikoBenchmarkTask }) {
    requireSkikoVersionForPublishedArtifacts()
}

val skikoWasmRuntimeVariant by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "skiko-runtime"))
    }
}

val skikoWasmRuntimeLegacy by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val benchmarkServerRuntimeClasspath by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    if (!isCompositeBuild) {
        skikoWasmRuntimeVariant(libs.skiko.wasm.js)
        skikoWasmRuntimeLegacy(libs.skiko.js.wasm.runtime)
    }

    benchmarkServerRuntimeClasspath("io.ktor:ktor-server-core-jvm:3.3.3")
    benchmarkServerRuntimeClasspath("io.ktor:ktor-server-netty-jvm:3.3.3")
    benchmarkServerRuntimeClasspath("io.ktor:ktor-server-cors-jvm:3.3.3")
    benchmarkServerRuntimeClasspath("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.9.0")
}

val unpackedWasmRuntime = layout.buildDirectory.dir("resources")

val unpackWasmRuntime = tasks.register("unpackWasmRuntime", Copy::class) {
    into(unpackedWasmRuntime)
    outputs.upToDateWhen { false }
    from(providers.provider {
        val runtimeFiles = if (isCompositeBuild) {
            files(gradle.includedBuild("skiko").projectDir.resolve("./build/libs/skiko-wasm-0.0.0-SNAPSHOT.jar"))
        } else {
            resolvePublishedWasmRuntime()
        }

        runtimeFiles.map { zipTree(it) }
    })

    if (isCompositeBuild) {
        dependsOn(gradle.includedBuild("skiko").task(":skikoWasmJar"))
    }
}

fun resolvePublishedWasmRuntime(): Set<File> {
    val variantResult = runCatching { skikoWasmRuntimeVariant.resolve() }
    if (variantResult.isSuccess) {
        return variantResult.getOrThrow()
    }

    val legacyResult = runCatching { skikoWasmRuntimeLegacy.resolve() }
    if (legacyResult.isSuccess) {
        return legacyResult.getOrThrow()
    }

    throw GradleException(
        "Could not resolve Skiko wasm runtime for ${skikoVersion.get()}.\n" +
            "Tried skiko-runtime variant from org.jetbrains.skiko:skiko-wasm-js:${skikoVersion.get()}:\n" +
            "${variantResult.exceptionOrNull()?.message}\n" +
            "Tried legacy org.jetbrains.skiko:skiko-js-wasm-runtime:${skikoVersion.get()}:\n" +
            "${legacyResult.exceptionOrNull()?.message}"
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    dependsOn(unpackWasmRuntime)
}

tasks.matching { it.name == "jsProcessResources" || it.name == "wasmJsProcessResources" }.configureEach {
    dependsOn(unpackWasmRuntime)
}

configurations.matching { it.name == "awtRuntimeClasspath" }.configureEach {
    attributes {
        attribute(
            OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE,
            objects.named(OperatingSystemFamily::class.java, hostOperatingSystemFamily)
        )
        attribute(
            MachineArchitecture.ARCHITECTURE_ATTRIBUTE,
            objects.named(MachineArchitecture::class.java, hostMachineArchitecture)
        )
    }
}

gradle.taskGraph.whenReady {
    if (allTasks.any { it.requiresSkikoVersion }) {
        requireSkikoVersionForPublishedArtifacts()
    }

    val appArgs = runArguments
        ?.split(" ")
        ?.filter { it.isNotBlank() }
        .orEmpty()
    val openBrowser = providers.gradleProperty("skiko.benchmark.openBrowser")
        .orElse("true")
        .get()
        .toBoolean()

    tasks.named<KotlinWebpack>("wasmJsBrowserProductionRun") {
        val args = appArgs
            .mapIndexed { index, arg -> "arg$index=${arg.replace(" ", "%20")}" }
            .joinToString("&")
        val url = if (args.isBlank()) {
            "http://localhost:8080"
        } else {
            "http://localhost:8080?$args"
        }

        devServerProperty = devServerProperty.get().copy(open = if (openBrowser) url else false)
    }
}

val Task.requiresSkikoVersion: Boolean
    get() = name in setOf(
        "awtBenchmark",
        "wasmJsBrowserProductionRun",
        "wasmJsBrowserDevelopmentRun",
        "runBrowserAndSaveStats",
    )

val String.referencesSkikoBenchmarkTask: Boolean
    get() {
        val taskName = substringAfterLast(":")
        return taskName in setOf(
            "awtBenchmark",
            "wasmJsBrowserProductionRun",
            "wasmJsBrowserDevelopmentRun",
            "runBrowserAndSaveStats",
        )
    }

kotlin {
    jvm("awt") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }

    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "skiko-benchmarks.js"
            }
        }
        binaries.executable()
    }

    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "skiko-benchmarks.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.skiko)
        }

        val awtMain by getting {
            dependencies {
                if (isCompositeBuild) {
                    runtimeOnly(files(localAwtRuntimeJar!!))
                } else {
                    implementation(hostAwtRuntimeArtifact)
                }
            }
        }

        val webMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.browser)
            }
            resources.srcDirs(unpackedWasmRuntime)
        }

        jsMain {
            dependsOn(webMain)
        }

        wasmJsMain {
            dependsOn(webMain)
        }
    }
}

tasks.register<JavaExec>("awtBenchmark") {
    group = "benchmark"
    description = "Runs Skiko API benchmarks on the JVM/AWT target."

    val mainCompilation = kotlin.targets.getByName("awt").compilations.getByName("main")
    dependsOn(mainCompilation.compileTaskProvider)
    if (isCompositeBuild) {
        dependsOn(gradle.includedBuild("skiko").task(":skikoJvmJarForTests"))
    }
    classpath = files(mainCompilation.output.allOutputs, mainCompilation.runtimeDependencyFiles)
    mainClass.set("org.jetbrains.skiko.benchmarks.JvmMainKt")
    args(runArguments?.split(" ")?.filter { it.isNotBlank() }.orEmpty())
}

tasks.register<Exec>("runBenchmarkServer") {
    group = "benchmark"
    description = "Runs the local Ktor server that receives browser benchmark reports."

    val serverArguments = providers.gradleProperty("benchmarkServer.arguments").orElse("")

    doFirst {
        commandLine(
            listOf(
                "kotlin",
                "-classpath",
                benchmarkServerRuntimeClasspath.asPath,
                projectDir.resolve("benchmark_server.main.kts").absolutePath,
            ) + serverArguments.get().split(" ").filter { it.isNotBlank() }
        )
    }
}

tasks.register("runBrowserAndSaveStats") {
    group = "benchmark"
    description = "Runs wasm browser benchmarks through run_benchmarks.main.kts."

    doLast {
        val script = projectDir.resolve("run_benchmarks.main.kts")
        val scriptArgs = mutableListOf(
            script.absolutePath,
            "web",
            "version=${skikoVersion.get()}"
        )
        if (isCompositeBuild) {
            scriptArgs += "skiko.composite.build=1"
            scriptArgs += "skiko.wasm.enabled=true"
            scriptArgs += "skiko.awt.enabled=false"
        }
        scriptArgs += runArguments?.split(" ")?.filter { it.isNotBlank() }.orEmpty()

        val process = ProcessBuilder(scriptArgs).directory(projectDir).inheritIO().start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw GradleException("run_benchmarks.main.kts failed with exit code $exitCode")
        }
    }
}
