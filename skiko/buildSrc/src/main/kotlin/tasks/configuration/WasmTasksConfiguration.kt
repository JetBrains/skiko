@file:OptIn(ExperimentalWasmDsl::class)

package tasks.configuration

import Arch
import CompileSkikoCppTask
import IMPORT_GENERATOR
import LinkSkikoWasmTask
import OS
import SkikoProjectContext
import compilerForTarget
import linkerForTarget
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import projectDirs
import registerOrGetSkiaDirProvider
import setupMjs
import setupReexportMjs
import skikoTestMjs
import supportWeb
import java.io.File

fun SkikoProjectContext.declareWasmTasks() {
    if (!project.supportWeb) {
        return
    }

    val skiaWasmDir = registerOrGetSkiaDirProvider(OS.Wasm, Arch.Wasm, false)
    val compileWasm by project.tasks.registering(CompileSkikoCppTask::class) {
        dependsOn(skiaWasmDir)

        compiler.set(compilerForTarget(OS.Wasm, Arch.Wasm))
        buildTargetOS.set(OS.Wasm)
        buildTargetArch.set(Arch.Wasm)
        buildVariant.set(buildType)

        val srcDirs = projectDirs("src/commonMain/cpp/common", "src/webMain/cpp", "src/nativeJsMain/cpp") +
                if (skiko.includeTestHelpers) projectDirs("src/nativeJsTest/cpp") else emptyList()
        sourceRoots.set(srcDirs)

        includeHeadersNonRecursive(project.projectDir.resolve("src/nativeJsMain/cpp"))
        includeHeadersNonRecursive(project.projectDir.resolve("src/webMain/cpp"))
        includeHeadersNonRecursive(project.projectDir.resolve("src/commonMain/cpp/common/include"))
        includeHeadersNonRecursive(skiaHeadersDirs(skiaWasmDir.get()))

        flags.set(
            buildList {
                addAll(skiaPreprocessorFlags(OS.Wasm, buildType))
                addAll(buildType.clangFlags)
                add("-fno-rtti")
                add("-fno-exceptions")
                if (skiko.isWasmBuildWithProfiling) add("--profiling")
            }
        )
    }

    fun LinkSkikoWasmTask.configureCommon(prefixPath: String) {
        dependsOn(compileWasm)
        dependsOn(skiaWasmDir)

        linker.set(linkerForTarget(OS.Wasm, Arch.Wasm))
        buildTargetOS.set(OS.Wasm)
        buildTargetArch.set(Arch.Wasm)
        buildVariant.set(buildType)

        libFiles = project.fileTree(skiaWasmDir.get()) { include("**/*.a") }
        objectFiles = project.fileTree(compileWasm.map { it.outDir.get() }) {
            include("**/*.o")
        }

        externPostJs.from(
            // the order matters
            project.layout.projectDirectory.file("src/webMain/resources/skikoCallbacks.js"),
            project.layout.projectDirectory.file(prefixPath)
        )

        flags.addAll(buildList {
            addAll(
                listOf(
                    "-l", "GL",
                    "-s", "MAX_WEBGL_VERSION=2",
                    "-s", "MIN_WEBGL_VERSION=2",
                    "-s", "OFFSCREEN_FRAMEBUFFER=1",
                    "-s", "ALLOW_MEMORY_GROWTH=1", // TODO: Is there a better way? Should we use `-s INITIAL_MEMORY=X`?
                    "-s", "EXPORT_ES6=1",
                    "-s", "MODULARIZE=1",
                    "-s", "EXPORT_NAME=loadSkikoWASM",
                    "-s", "EXPORTED_RUNTIME_METHODS=\"[GL, wasmExports]\"",
                    "-s", "SUPPORT_LONGJMP=wasm",
                    "--bind",
                    // -O2 saves 800kB for the output file, and ~100kB for transferred size.
                    // -O3 breaks the exports in js/mjs files. skiko.wasm size is the same though
                    "-O2"
                )
            )

            if (skiko.isWasmBuildWithProfiling) add("--profiling")
        })

        doLast {
            // skiko.mjs is referenced in karma.config.d/*/config.js
            // so symbols must be replaced right after linking
            val jsFile = outDir.asFile.get().walk().first { it.name == jsOutputFileName.get() }

            val isEnvironmentNodeCheckRegex = Regex(
                // spacing is different in release and debug builds
                """if\s*\(ENVIRONMENT_IS_NODE\)\s*\{"""
            )

            val originalContent = jsFile.readText()
            val newContent = originalContent
                .replace(isEnvironmentNodeCheckRegex, "if (false) {") // to make webpack erase this part
            jsFile.writeText(newContent)
        }
    }

    val linkWasm by project.tasks.registering(LinkSkikoWasmTask::class) {
        dependsOn(
            kotlin.wasmJs().compilations["main"].compileTaskProvider,
            kotlin.js().compilations["main"].compileTaskProvider
        )

        buildSuffix.set("es6")
        jsOutputFileName.set("skiko.mjs") // this determines the name .wasm file too
        libOutputFileName.set("skiko.wasm")

        configureCommon(project.setupMjs.normalize().absolutePath)
    }

    val linkWasmD8WithES6 by project.tasks.registering(LinkSkikoWasmTask::class) {
        dependsOn(
            kotlin.wasmJs().compilations["main"].compileTaskProvider,
            kotlin.js().compilations["main"].compileTaskProvider
        )

        buildSuffix.set("d8")
        jsOutputFileName.set("skikod8.mjs") // this determines the name .wasm file too
        libOutputFileName.set("skikod8.wasm")

        flags.addAll(listOf("-s", "ENVIRONMENT=shell"))

        configureCommon(project.setupMjs.normalize().absolutePath)
    }

    // skikoWasmJar is used by task name
    val skikoWasmJar by project.tasks.registering(Jar::class) {
        // We produce jar that contains .js of wrapper/bindings and .wasm with Skia + bindings.
        from(project.setupReexportMjs.parentFile) {
            include(project.setupReexportMjs.name)
        }

        from(linkWasm) {
            include("*.wasm")
            include("*.mjs")
        }

        from(linkWasmD8WithES6) {
            include("*.mjs")
            filesMatching("*.mjs") {
                filter { it.replace("skikod8.wasm", "skiko.wasm") }
            }
        }

        archiveBaseName.set("skiko-wasm")
        doLast {
            println("Wasm and JS at: ${archiveFile.get().asFile.absolutePath}")
        }
    }
}

abstract class AbstractImportGeneratorCompilerPluginSupportPlugin(
    val compilationName: String,
    private val outputFileProvider: (Project) -> File,
    private val prefixFileProvider: (Project) -> File,
    private val reexportFileProvider: ((Project) -> File)?
) : KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        val outputFile = outputFileProvider(project)
        val prefixFile = prefixFileProvider(project)
        val reexportFile = reexportFileProvider?.invoke(project)

        return project.provider {
            buildList {
                add(SubpluginOption("import-generator-path", outputFile.normalize().absolutePath))
                add(SubpluginOption("import-generator-prefix", prefixFile.normalize().absolutePath),)
                if (reexportFile != null) {
                    add(SubpluginOption("import-generator-reexport-path", reexportFile.normalize().absolutePath))
                }
            }
        }
    }

    override fun getCompilerPluginId() = "org.jetbrains.skiko.imports.generator"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(SkikoArtifacts.groupId, IMPORT_GENERATOR)

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return ((kotlinCompilation.platformType == KotlinPlatformType.wasm) || (kotlinCompilation.platformType == KotlinPlatformType.js))
                && kotlinCompilation.name == compilationName
    }
}

class WasmImportsGeneratorCompilerPluginSupportPlugin : AbstractImportGeneratorCompilerPluginSupportPlugin(
    KotlinCompilation.MAIN_COMPILATION_NAME,
    { it.setupMjs },
    { it.projectDir.resolve("src/webMain/resources/pre-setup.mjs") },
    { it.setupReexportMjs }
)

class WasmImportsGeneratorForTestCompilerPluginSupportPlugin : AbstractImportGeneratorCompilerPluginSupportPlugin(
    KotlinCompilation.TEST_COMPILATION_NAME,
    { it.skikoTestMjs },
    { it.projectDir.resolve("src/webMain/resources/pre-skiko-test.mjs") },
    null
)

fun KotlinJsTargetDsl.setupImportsGeneratorPlugin() {
    val main by compilations.getting
    val test by compilations.getting

    main.compileTaskProvider.configure {
        outputs.file(project.setupMjs)
    }

    test.compileTaskProvider.configure {
        outputs.file(project.skikoTestMjs)
    }

    listOf(main, test).forEach {
        // By default, it will try to use the same version as kotlin, because we use version=null in getPluginArtifact.
        // But we don't publish the artifact, therefore we substitute it for project dependency.
        it.configurations.pluginConfiguration.resolutionStrategy.dependencySubstitution {
            substitute(module("${SkikoArtifacts.groupId}:$IMPORT_GENERATOR"))
                .using(project(":import-generator"))
        }
    }
}
