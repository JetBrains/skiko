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
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import projectDirs
import registerOrGetSkiaDirProvider
import setupMjs
import skikoTestMjs
import supportJs
import supportWasm
import java.io.File

data class LinkWasmTasks(
    val linkWasm: TaskProvider<LinkSkikoWasmTask>?,
    val linkWasmWithES6: TaskProvider<LinkSkikoWasmTask>?
)

fun SkikoProjectContext.createWasmLinkTasks(): LinkWasmTasks = with(this.project) {
    if (!supportWasm && !supportJs) {
        return LinkWasmTasks(null, null)
    }
    val skiaWasmDir = registerOrGetSkiaDirProvider(OS.Wasm, Arch.Wasm, false)
    val compileWasm by tasks.registering(CompileSkikoCppTask::class) {
        val osArch = OS.Wasm to Arch.Wasm

        dependsOn(skiaWasmDir)

        compiler.set(compilerForTarget(OS.Wasm, Arch.Wasm))
        buildTargetOS.set(osArch.first)
        buildTargetArch.set(osArch.second)
        buildVariant.set(buildType)

        val srcDirs = projectDirs("src/commonMain/cpp/common", "src/jsWasmMain/cpp", "src/nativeJsMain/cpp") +
                if (skiko.includeTestHelpers) projectDirs("src/nativeJsTest/cpp") else emptyList()
        sourceRoots.set(srcDirs)

        includeHeadersNonRecursive(projectDir.resolve("src/nativeJsMain/cpp"))
        includeHeadersNonRecursive(projectDir.resolve("src/jsWasmMain/cpp"))
        includeHeadersNonRecursive(projectDir.resolve("src/commonMain/cpp/common/include"))
        includeHeadersNonRecursive(skiaHeadersDirs(skiaWasmDir.get()))

        flags.set(
            mutableListOf<String?>().apply {
                addAll(skiaPreprocessorFlags(OS.Wasm, buildType))
                addAll(buildType.clangFlags)
                add("-fno-rtti")
                add("-fno-exceptions")
                if (skiko.isWasmBuildWithProfiling) add("--profiling")
            }
        )
    }

    val configureCommon: LinkSkikoWasmTask.(outputES6: Boolean, prefixPath: String, forD8: Boolean) -> Unit = { outputES6, prefixPath, isForD8 ->
        val osArch = OS.Wasm to Arch.Wasm

        dependsOn(compileWasm)
        dependsOn(skiaWasmDir)
        val unpackedSkia = skiaWasmDir.get()

        linker.set(linkerForTarget(OS.Wasm, Arch.Wasm))
        buildTargetOS.set(osArch.first)
        buildTargetArch.set(osArch.second)
        buildVariant.set(buildType)
        if (outputES6) buildSuffix.set("es6")
        if (isForD8) buildSuffix.set("d8")

        libFiles = project.fileTree(unpackedSkia) { include("**/*.a") }
        objectFiles = project.fileTree(compileWasm.map { it.outDir.get() }) {
            include("**/*.o")
        }

        val wasmFileName = if (outputES6) {
            if (isForD8) "skikod8.wasm" else "skikomjs.wasm"
        } else {
            "skiko.wasm" // to keep it compatible with older apps
        }
        val jsFileName = if (outputES6) {
            if (isForD8) "skikod8.mjs" else "skikomjs.mjs"
        } else {
            "skiko.js" // to keep it compatible with older apps
        }
        libOutputFileName.set(wasmFileName) // emcc ignores this, it names .wasm file identically to js output
        jsOutputFileName.set(jsFileName) // this determines the name .wasm file too

        skikoJsPrefix.from(
            // the order matters
            project.layout.projectDirectory.file("src/jsWasmMain/resources/skikoCallbacks.js"),
            project.layout.projectDirectory.file(prefixPath)
        )

        @OptIn(kotlin.ExperimentalStdlibApi::class)
        flags.set(mutableListOf<String?>().apply {
            addAll(
                listOf(
                    "-l", "GL",
                    "-s", "MAX_WEBGL_VERSION=2",
                    "-s", "MIN_WEBGL_VERSION=2",
                    "-s", "OFFSCREEN_FRAMEBUFFER=1",
                    "-s", "MALLOC=mimalloc",
                    "-s", "ALLOW_MEMORY_GROWTH=1", // TODO: Is there a better way? Should we use `-s INITIAL_MEMORY=X`?
                    "--bind",
                    // -O2 saves 800kB for the output file, and ~100kB for transferred size.
                    // -O3 breaks the exports in js/mjs files. skiko.wasm size is the same though
                    "-O2"
                )
            )
            addAll(listOf("-s", "SUPPORT_LONGJMP=wasm"))
            if (outputES6) {
                addAll(
                    listOf(
                        "-s", "EXPORT_ES6=1",
                        "-s", "MODULARIZE=1",
                        "-s", "EXPORT_NAME=loadSkikoWASM",
                        "-s", "EXPORTED_RUNTIME_METHODS=\"[GL, wasmExports]\"",
                        // "-s", "EXPORT_ALL=1",
                    )
                )
                if (isForD8) {
                    addAll(listOf("-s", "ENVIRONMENT=shell"))
                }
            }

            if (skiko.isWasmBuildWithProfiling) add("--profiling")
        })

        doLast {
            // skiko.js (and skiko.mjs) files are directly referenced in karma.config.d/*/config.js
            // so symbols must be replaced right after linking
            val jsFiles = outDir.asFile.get().walk()
                .filter { it.isFile && (it.name.endsWith(".js") || it.name.endsWith(".mjs")) }

            val isEnvironmentNodeCheckRegex = Regex(
                // spaces are different in release and debug builds
                """if\s*\(ENVIRONMENT_IS_NODE\)\s*\{"""
            )

            for (jsFile in jsFiles) {
                val originalContent = jsFile.readText()
                val newContent = originalContent.replace("_org_jetbrains", "org_jetbrains")
                    .replace("skikomjs.wasm", "skiko.wasm")
                    .replace("skikod8.wasm", "skiko.wasm")
                    .replace(isEnvironmentNodeCheckRegex, "if (false) {") // to make webpack erase this part
                jsFile.writeText(newContent)

                if (outputES6) {
                    // delete this file as its presence can be confusing.
                    // It's identical to skiko.wasm and we use skiko.wasm in `skikoWasmJar`task
                    outDir.file(wasmFileName).get().asFile.delete()

                    val renameTo = if (isForD8) "skikod8.mjs" else "skiko.mjs"
                    outDir.file(jsFileName).get().asFile.renameTo(outDir.asFile.get().resolve(renameTo))
                }
            }
        }
    }

    val linkWasmWithES6 by tasks.registering(LinkSkikoWasmTask::class) {
        val wasmJsTarget = kotlin.wasmJs()
        val main by wasmJsTarget.compilations
        dependsOn(main.compileTaskProvider)
        configureCommon(true, setupMjs.normalize().absolutePath, false)
    }

    val linkWasmD8WithES6 by tasks.registering(LinkSkikoWasmTask::class) {
        val wasmJsTarget = kotlin.wasmJs()
        val main by wasmJsTarget.compilations
        dependsOn(main.compileTaskProvider)
        configureCommon(true, setupMjs.normalize().absolutePath, true)
    }

    val linkWasm by tasks.registering(LinkSkikoWasmTask::class) {
        dependsOn(linkWasmWithES6)
        configureCommon(false, "src/jsWasmMain/resources/setup.js", false)
    }

    // skikoWasmJar is used by task name
    val skikoWasmJar by project.tasks.registering(Jar::class) {
        dependsOn(linkWasm)
        // We produce jar that contains .js of wrapper/bindings and .wasm with Skia + bindings.
        val wasmOutDir = linkWasm.map { it.outDir }
        val wasmEsOutDir = linkWasmWithES6.map { it.outDir }
        val wasmD8OutDir = linkWasmD8WithES6.map { it.outDir }

        from(wasmOutDir) {
            include("*.wasm")
            include("*.js")
            include("*.mjs")
        }
        from(wasmEsOutDir) {
            include("*.mjs")
        }
        from(wasmD8OutDir) {
            include("*.mjs")
        }

        archiveBaseName.set("skiko-wasm")
        doLast {
            println("Wasm and JS at: ${archiveFile.get().asFile.absolutePath}")
        }
    }

    return LinkWasmTasks(linkWasm, linkWasmWithES6)
}

abstract class AbstractImportGeneratorCompilerPluginSupportPlugin(
    val compilationName: String,
    val outputFileProvider: (Project) -> File,
    val prefixFileProvider: (Project) -> File
) : KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        val outputFile = outputFileProvider(project)
        val prefixFile = prefixFileProvider(project)

        return project.provider {
            listOf(
                SubpluginOption("import-generator-path", outputFile.normalize().absolutePath),
                SubpluginOption("import-generator-prefix", prefixFile.normalize().absolutePath)
            )
        }
    }

    override fun getCompilerPluginId() = "org.jetbrains.skiko.imports.generator"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(SkikoArtifacts.groupId, IMPORT_GENERATOR)

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return kotlinCompilation.platformType == KotlinPlatformType.wasm
                && kotlinCompilation.name == compilationName
    }
}

class WasmImportsGeneratorCompilerPluginSupportPlugin : AbstractImportGeneratorCompilerPluginSupportPlugin(
    KotlinCompilation.MAIN_COMPILATION_NAME,
    { it.setupMjs },
    { it.projectDir.resolve("src/jsWasmMain/resources/pre-setup.mjs") }
)

class WasmImportsGeneratorForTestCompilerPluginSupportPlugin : AbstractImportGeneratorCompilerPluginSupportPlugin(
    KotlinCompilation.TEST_COMPILATION_NAME,
    { it.skikoTestMjs },
    { it.projectDir.resolve("src/jsWasmMain/resources/pre-skiko-test.mjs") }
)

fun KotlinWasmJsTargetDsl.setupImportsGeneratorPlugin() {
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