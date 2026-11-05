@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsPlugin
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

allprojects {
    // TODO https://youtrack.jetbrains.com/issue/CMP-11110 Remove compromised root certificate workaround after updating on Kotlin 2.5.0
    // Override the node version to fix https://youtrack.jetbrains.com/issue/CMP-11011
    // Workaround is from https://youtrack.jetbrains.com/issue/KT-90732
    val subproject = this
    subproject.plugins.withType<NodeJsPlugin>().configureEach {
        subproject.extensions.getByType(NodeJsEnvSpec::class.java).version.set("26.12.1")
    }
    subproject.plugins.withType<WasmNodeJsPlugin>().configureEach {
        subproject.extensions.getByType(WasmNodeJsEnvSpec::class.java).version.set("26.12.1")
    }
}

repositories {
    mavenCentral {
        url = uri("https://cache-redirector.jetbrains.com/maven-central")
    }
    maven("https://redirector.kotlinlang.org/maven/compose-dev")
    mavenLocal()
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
