@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

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


val installNodeWindowedDeps = tasks.register<Exec>("installNodeWindowedDeps") {
    group = "NodeJs"
    description = "Installs the sample-only Node dependencies for the Skiko Node windowed runner."
    inputs.file(layout.projectDirectory.file("package.json"))
    inputs.file(layout.projectDirectory.file("package-lock.json"))
    inputs.property("useCpuCopy", providers.environmentVariable("SKIKO_NODE_CPU_COPY").orElse("1"))
    inputs.file(layout.projectDirectory.file("scripts/patch-node-gles-webgl2-window-surface.mjs"))
    inputs.property("npmExecutable", npmExecutable)
    outputs.dir(layout.projectDirectory.dir("node_modules/node-gles-webgl2"))
    outputs.dir(layout.projectDirectory.dir("node_modules/@kmamal/sdl"))
    environment("CXXFLAGS", "-std=c++20")
    commandLine(npmExecutable.get(), "ci", "--no-audit", "--no-fund")
}

val patchNodeWindowedDeps = tasks.register<Exec>("patchNodeWindowedDeps") {
    group = "NodeJs"
    description = "Patches and rebuilds node-gles-webgl2 for native SDL window-surface presentation."
    dependsOn(installNodeWindowedDeps)
    onlyIf { providers.environmentVariable("SKIKO_NODE_CPU_COPY").orElse("1").get() != "1" }
    inputs.file(layout.projectDirectory.file("scripts/patch-node-gles-webgl2-window-surface.mjs"))
    inputs.property("nodeExecutable", nodeExecutable)
    outputs.dir(layout.projectDirectory.dir("node_modules/node-gles-webgl2"))
    commandLine(nodeExecutable.get(), "scripts/patch-node-gles-webgl2-window-surface.mjs")
}

val rebuildNodeWindowedDeps = tasks.register<Exec>("rebuildNodeWindowedDeps") {
    group = "NodeJs"
    description = "Rebuilds node-gles-webgl2 after applying the native SDL window-surface patch."
    dependsOn(patchNodeWindowedDeps)
    onlyIf { providers.environmentVariable("SKIKO_NODE_CPU_COPY").orElse("1").get() != "1" }
    inputs.property("npmExecutable", npmExecutable)
    outputs.dir(layout.projectDirectory.dir("node_modules/node-gles-webgl2"))
    environment("CXXFLAGS", "-std=c++20")
    commandLine(npmExecutable.get(), "rebuild", "node-gles-webgl2")
}

tasks.register<Exec>("skikoNodeWindowedRun") {
    group = "application"
    description = "Builds and runs the Skiko WASM sample under Node.js in a native SDL window."
    dependsOn("wasmJsProductionExecutableCompileSync", installNodeWindowedDeps, rebuildNodeWindowedDeps)
    inputs.property("nodeExecutable", nodeExecutable)
    environment(
        "SKIKO_NODE_CPU_COPY",
        providers.environmentVariable("SKIKO_NODE_CPU_COPY").orElse("1").get()
    )
    commandLine(nodeExecutable.get(), "node-runner.mjs")
}

kotlin {

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
        commonMain.dependencies {
            implementation(libs.skiko)
        }

        webMain {
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
