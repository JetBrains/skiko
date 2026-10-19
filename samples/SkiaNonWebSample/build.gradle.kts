import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral {
        url = uri("https://cache-redirector.jetbrains.com/maven-central")
    }
    maven("https://redirector.kotlinlang.org/maven/compose-dev")
    mavenLocal()
}

val nodeExecutable = providers.gradleProperty("skiko.node.executable").orElse("node")
val npmExecutable = providers.gradleProperty("skiko.npm.executable").orElse("npm")
val cmakeExecutable = providers.gradleProperty("skiko.cmake.executable").orElse("cmake")

val skiaDir = providers.gradleProperty("skia.dir")
    .orElse(providers.environmentVariable("SKIA_DIR"))
    .orElse(providers.environmentVariable("SKIA_ROOT"))

val wasmtimeRoot = providers.gradleProperty("wasmtime.root")
    .orElse(providers.environmentVariable("WASMTIME_ROOT"))
    .orElse(providers.environmentVariable("WASMTIME_DIR"))

val skiaLibrary = providers.gradleProperty("skia.library")
    .orElse(providers.environmentVariable("SKIA_LIBRARY"))

val installNativeSkikoDeps = tasks.register<Exec>("installNativeSkikoDeps") {
    group = "NodeJs"
    description = "Installs node-gyp for the native Skiko bridge."
    workingDir(layout.projectDirectory.dir("native-skiko"))
    inputs.file(layout.projectDirectory.file("native-skiko/package.json"))
    inputs.file(layout.projectDirectory.file("native-skiko/package-lock.json"))
    inputs.property("npmExecutable", npmExecutable)
    outputs.dir(layout.projectDirectory.dir("native-skiko/node_modules"))
    commandLine(npmExecutable.get(), "ci", "--no-audit", "--no-fund", "--ignore-scripts")
}

val buildNativeSkiko = tasks.register<Exec>("buildNativeSkiko") {
    group = "NodeJs"
    description = "Builds the native Skiko OpenGL and SDL presentation bridge."
    dependsOn(installNativeSkikoDeps)
    workingDir(layout.projectDirectory.dir("native-skiko"))
    inputs.file(layout.projectDirectory.file("native-skiko/binding.cc"))
    inputs.file(layout.projectDirectory.file("native-skiko/binding.gyp"))
    inputs.files(layout.projectDirectory.dir("native-skiko").asFileTree.matching {
        include("*.inc")
    })
    inputs.property("npmExecutable", npmExecutable)
    if (skiaDir.isPresent) {
        inputs.property("skiaDir", skiaDir)
        environment("SKIA_DIR", skiaDir.get())
    }
    if (skiaLibrary.isPresent) {
        inputs.property("skiaLibrary", skiaLibrary)
        environment("SKIA_LIBRARY", skiaLibrary.get())
    }
    outputs.file(layout.projectDirectory.file("native-skiko/build/Release/native_skiko.node"))
    commandLine(npmExecutable.get(), "run", "build")
}

val configureWasmtimeHost = tasks.register<Exec>("configureWasmtimeHost") {
    group = "wasmtime"
    description = "Configures CMake build for the native Skiko Wasmtime host runner."
    workingDir(layout.projectDirectory.dir("wasmtime-skiko"))
    onlyIf {
        !layout.projectDirectory.file("wasmtime-skiko/build/CMakeCache.txt").asFile.exists()
    }
    val cmakeArgs = mutableListOf(cmakeExecutable.get(), "-S", ".", "-B", "build")
    if (skiaDir.isPresent) {
        cmakeArgs.add("-DSKIA_ROOT=${skiaDir.get()}")
    }
    if (wasmtimeRoot.isPresent) {
        cmakeArgs.add("-DWASMTIME_ROOT=${wasmtimeRoot.get()}")
    }
    if (skiaLibrary.isPresent) {
        cmakeArgs.add("-DSKIA_LIBRARY=${skiaLibrary.get()}")
    }
    commandLine(cmakeArgs)
}

val buildNativeWasmtime = tasks.register<Exec>("buildNativeWasmtime") {
    group = "wasmtime"
    description = "Builds the native Skiko Wasmtime host runner executable."
    dependsOn(configureWasmtimeHost)
    workingDir(layout.projectDirectory.dir("wasmtime-skiko"))
    inputs.dir(layout.projectDirectory.dir("wasmtime-skiko/src"))
    inputs.dir(layout.projectDirectory.dir("wasmtime-skiko/generated"))
    inputs.file(layout.projectDirectory.file("wasmtime-skiko/CMakeLists.txt"))
    outputs.file(layout.projectDirectory.file("wasmtime-skiko/build/skiko-wasmtime"))
    commandLine(cmakeExecutable.get(), "--build", "build", "-j")
}

val wasmJsNodeRun = tasks.register<Exec>("wasmJsNodeRun") {
    group = "application"
    description = "Builds and runs the Skiko WASM sample under Node.js in a native SDL window."
    dependsOn("wasmJsProductionExecutableCompileSync", buildNativeSkiko)
    inputs.property("nodeExecutable", nodeExecutable)
    commandLine(nodeExecutable.get(), "node-runner.mjs")
}

val wasmJsWasmtimeRun = tasks.register<Exec>("wasmJsWasmtimeRun") {
    group = "application"
    description = "Builds and runs the Skiko WASM sample under Wasmtime host in a native SDL window."
    dependsOn("wasmJsProductionExecutableCompileSync", buildNativeWasmtime)
    workingDir(layout.projectDirectory)
    val coreWasm = layout.projectDirectory.file("build/wasm/packages/SkiaNonWebSample/kotlin/skiko.wasm")
    val appWasm = layout.projectDirectory.file("build/wasm/packages/SkiaNonWebSample/kotlin/SkiaNonWebSample.wasm")
    val wasmtimeExecutable = layout.projectDirectory.file("wasmtime-skiko/build/skiko-wasmtime")
    commandLine(wasmtimeExecutable.asFile.absolutePath, coreWasm.asFile.absolutePath, appWasm.asFile.absolutePath)
}

tasks.register("skikoNodeWindowedRun") {
    group = "application"
    description = "Alias for wasmJsNodeRun."
    dependsOn(wasmJsNodeRun)
}

kotlin {

    wasmJs {
        compilerOptions {
            freeCompilerArgs.add("-Xwasm-use-new-exception-proposal")
        }

        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.skiko)
        }

        wasmJsMain {
            dependencies {
                implementation(libs.browser)
            }
        }
    }

    targets.withType<KotlinJsIrTarget>().all { configureSkikoWebRuntime(project, this) }
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

    val unpackRuntime = project.tasks.register(
        "unpackSkikoRuntimeFor$titledTargetName",
        UnpackSkikoRuntimeTask::class.java,
    ) {
        runtimeFiles.from(skikoWebRuntimeJarFiles)
        outputDirectory.set(unpackedRuntimeDir)
    }

    target.compilations.all {
        if (target.wasmTargetType != null) {
            binaries.all {
                linkSyncTask.configure {
                    dependsOn(unpackRuntime)
                    from.from(unpackedRuntimeDir)
                }
            }
        } else {
            project.tasks.named(processResourcesTaskName, ProcessResources::class.java) {
                from(unpackedRuntimeDir)
                dependsOn(unpackRuntime)
                exclude("META-INF")
            }
        }
    }
}

@CacheableTask
abstract class UnpackSkikoRuntimeTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val runtimeFiles: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:javax.inject.Inject
    abstract val archiveOperations: ArchiveOperations

    @get:javax.inject.Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun unpack() {
        fileSystemOperations.copy {
            from(runtimeFiles.files.map(archiveOperations::zipTree))
            into(outputDirectory)
            exclude("META-INF/**")
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }
}
