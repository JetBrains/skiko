package tasks.configuration

import Arch
import CompileSkikoCppTask
import CopyEmscriptenWebGLLibsTask
import GenerateWasmSideModuleExportsTask
import SetupEmscriptenTask
import IMPORT_GENERATOR
import LinkSkikoWasmTask
import OS
import OptimizeSkikoWasmTask
import SetupWasiSdkTask
import SkikoModuleKind
import SkikoProjectContext
import compilerForTarget
import dsl.TargetEnv
import hostOs
import linkerForTarget
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import projectDirs
import registerOrGetSkiaDirProvider
import resolveSdkDir
import supportWeb
import wasmImports
import wasmImport
import wasiSdkExecutableName
import java.io.File

private val Project.setupMjs
    get() = wasmImport("setup.mjs")

// import-generator inlines local JS imports relative to the prefix file,
// so the prefix and generated Emscripten WebGL files must share this directory.
private val Project.generatedPreSetupMjs
    get() = layout.buildDirectory.file("generated/emscriptenWebGLLibs/webMain/pre-setup.mjs").get().asFile

private fun Project.sideModuleSetupMjs(libBaseName: String) =
    wasmImport("$libBaseName.mjs")

private fun Project.setupReexportMjs(libBaseName: String) =
    wasmImport("js-$libBaseName-reexport-symbols.mjs")

private val Project.wasmExportSymbols
    get() = wasmImport("required-wasm-exports.txt")

private val Project.wasmTestExportSymbols
    get() = wasmImport("required-wasm-test-exports.txt")

private fun Project.skikoTestMjs(libBaseName: String) =
    wasmImport("$libBaseName-test.mjs")

private fun Project.wasmSideModuleExportSymbols(mainLinkTaskName: String) =
    wasmImport("required-wasm-side-module-exports-$mainLinkTaskName.txt")

private val wasmSideModuleLinkTaskAttribute =
    Attribute.of("org.jetbrains.skiko.wasmSideModule.linkTask", String::class.java)

private const val WASM_SIDE_MODULE_USAGE = "skiko-wasm-side-module"
private const val WASM_TEST_RESOURCES_USAGE = "skiko-wasm-test-resources"

fun SkikoProjectContext.declareWasmTasks() {
    if (!project.supportWeb) {
        return
    }
    val isSideModule = kind == SkikoModuleKind.EXTENSION

    val emsdkVersion = project.findProperty("skiko.emsdk.version") ?:
        throw GradleException("skiko.emsdk.version property is not set")
    val setupEmscripten by project.tasks.registering(SetupEmscriptenTask::class) {
        sdkVersion.set(emsdkVersion.toString())
        project.findProperty("skiko.emsdk.dir")?.toString()?.let {
            sdkDir.set(project.layout.dir(project.provider { project.resolveSdkDir(it) }))
            requireExistingSdk.set(true)
        }
    }

    val wasiSdkVersion = project.findProperty("skiko.wasi.sdk.version") ?:
        throw GradleException("skiko.wasi.sdk.version property is not set")
    val setupWasiSdk by project.tasks.registering(SetupWasiSdkTask::class) {
        sdkVersion.set(wasiSdkVersion.toString())
        (project.findProperty("wasi.sdk")?.toString()
            ?: project.findProperty("skiko.wasi.sdk.dir")?.toString())?.let {
            sdkDir.set(project.layout.dir(project.provider { project.resolveSdkDir(it) }))
            requireExistingSdk.set(true)
        }
    }

    fun emscriptenBinaryToolPath(toolName: String) =
        setupEmscripten.flatMap { it.sdkDir.dir("upstream/bin") }.map {
            it.file(toolName).asFile.absolutePath
        }

    fun wasiSdkBinaryToolPath(toolName: String) =
        setupWasiSdk.flatMap { it.sdkDir.dir("bin") }.map {
            it.file(wasiSdkExecutableName(toolName)).asFile.absolutePath
        }

    val copyEmscriptenWebGLLibs = if (!isSideModule) {
        project.tasks.register<CopyEmscriptenWebGLLibsTask>("copyEmscriptenWebGLLibs") {
            dependsOn(setupEmscripten)
            nodeExecutable.set(project.layout.file(setupEmscripten.map { it.nodeExecutableFile() }))
            preprocessor.set(setupEmscripten.flatMap { it.sdkDir.file("upstream/emscripten/tools/preprocessor.mjs") })
            emscriptenLibDir.set(setupEmscripten.flatMap { it.sdkDir.dir("upstream/emscripten/src/lib") })
            outputDir.set(project.layout.buildDirectory.dir("generated/emscriptenWebGLLibs/webMain"))
            libFiles.set(listOf("libwebgl.js", "libwebgl2.js"))
            prefixFile.set(project.layout.projectDirectory.file("src/webMain/resources/pre-setup.mjs"))
            localImportFiles.from(project.layout.projectDirectory.file("src/webMain/resources/emscripten-compat.js"))
        }.also { task ->
            project.tasks.matching {
                it.name in listOf("compileKotlinJs", "compileKotlinWasmJs")
            }.configureEach {
                // The compiler plugin reads generatedPreSetupMjs while producing setup.mjs.
                dependsOn(task)
                inputs.dir(task.flatMap { it.outputDir })
                    .withPathSensitivity(PathSensitivity.RELATIVE)
            }
        }
    } else {
        null
    }

    val skiaWasmDir = registerOrGetSkiaDirProvider(OS.Wasm, Arch.Wasm, false)
    val compileWasm by project.tasks.registering(CompileSkikoCppTask::class) {
        dependsOn(setupWasiSdk)
        dependsOn(skiaWasmDir)
        compiler.set(wasiSdkBinaryToolPath(compilerForTarget(OS.Wasm, Arch.Wasm)))
        buildTargetOS.set(OS.Wasm)
        buildTargetArch.set(Arch.Wasm)
        buildVariant.set(buildType)

        val srcDirs = projectDirs("src/commonMain/cpp/common", "src/webMain/cpp", "src/nativeJsMain/cpp") +
                if (skiko.includeTestHelpers) projectDirs("src/nativeJsTest/cpp") else emptyList()
        sourceRoots.set(srcDirs)

        includeHeadersNonRecursive(project.projectDir.resolve("src/nativeJsMain/cpp"))
        includeHeadersNonRecursive(project.projectDir.resolve("src/webMain/cpp"))
        includeHeadersNonRecursive(project.projectDir.resolve("src/commonMain/cpp/common/include"))
        if (isSideModule) {
            val coreProjectDir = project.rootProject.projectDir
            includeHeadersNonRecursive(coreProjectDir.resolve("src/nativeJsMain/cpp"))
            includeHeadersNonRecursive(coreProjectDir.resolve("src/commonMain/cpp/common/include"))
        }
        includeHeadersNonRecursive(skiaHeadersDirs(skiaWasmDir.get()))

        flags.set(
            buildList {
                addAll(skiaPreprocessorFlags(OS.Wasm, buildType)) // Skia/ICU feature macros for this WASM build type.
                addAll(buildType.clangFlags) // C++ standard plus Debug/Release optimization or debug-info flags.
                add("-O2") // Optimize for speed without the most expensive optimization passes.
                add("-flto") // Enable link-time optimization.
                add("-fvisibility=hidden") // Hide symbols by default unless explicitly exported.
                add("-fno-rtti") // Disable C++ runtime type information.
                add("-fno-exceptions") // Disable C++ exception support.
                add("-fPIC") // Generate position-independent code.
                add("-D_WASI_EMULATED_MMAN") // Enable WASI libc's minimal mmap emulation declarations.
                add("-D_WASI_EMULATED_SIGNAL") // Enable WASI libc's minimal signal emulation declarations.
                add("-D_WASI_EMULATED_PROCESS_CLOCKS") // Enable WASI libc process-clock emulation declarations.
                add("-D_WASI_EMULATED_GETPID") // Enable WASI libc getpid() emulation declarations.
                add("-mllvm") // Forward the next option directly to LLVM.
                add("-wasm-enable-sjlj") // Enable LLVM's WebAssembly setjmp/longjmp lowering pass.
                add("-mexception-handling") // Enable WASM EH support used by SjLj; C++ exceptions stay disabled.
                if (skiko.isWasmBuildWithProfiling) add("--profiling") // Keep function names for profiling output.
            }
        )
    }

    fun LinkSkikoWasmTask.configureCommon(prefixPath: String) {
        copyEmscriptenWebGLLibs?.let { dependsOn(it) }
        dependsOn(setupWasiSdk)
        dependsOn(compileWasm)
        dependsOn(skiaWasmDir)
        val skiaBinDir = skiaWasmDir.get().resolve("out/${buildType.id}-wasm-wasm").absolutePath
        val resolvedBinaryInputs = resolveBinaryInputs(OS.Wasm, Arch.Wasm, TargetEnv.WASM, skiaBinDir)

        linker.set(wasiSdkBinaryToolPath(linkerForTarget(OS.Wasm, Arch.Wasm)))
        buildTargetOS.set(OS.Wasm)
        buildTargetArch.set(Arch.Wasm)
        buildVariant.set(buildType)

        libFiles = project.files(resolvedBinaryInputs.staticArchivePaths.distinct())
        objectFiles = project.fileTree(compileWasm.map { it.outDir.get() }) {
            include("**/*.o")
        }

        externPostJs.from(
            // the order matters
            project.rootProject.layout.projectDirectory.file("src/webMain/resources/skikoCallbacks.js"),
            project.layout.projectDirectory.file(prefixPath)
        )

        val exportsProvider = project.provider {
            val exportsFile = project.wasmExportSymbols
            if (!exportsFile.exists()) {
                throw GradleException("Required WASM exports file was not generated: ${exportsFile.absolutePath}")
            }
            val testExportsFile = project.wasmTestExportSymbols
            val testExports = if (skiko.includeTestHelpers) {
                if (!testExportsFile.exists()) {
                    throw GradleException("Required WASM test exports file was not generated: ${testExportsFile.absolutePath}")
                }
                testExportsFile.readLines()
            } else {
                emptyList()
            }
            val generatedExports = exportsFile.readLines() +
                    testExports +
                    // Side modules are loaded dynamically, so the main module must export
                    // only the symbols those modules actually import. This keeps the wasm
                    // smaller than --export-dynamic while still satisfying runtime linking.
                    project.wasmSideModuleExportSymbols(name)
                        .takeIf { it.exists() }
                        ?.readLines()
                        .orEmpty()
            (generatedExports + listOf(
                "malloc",
                "free",
                "memory",
                "__wasm_call_ctors",
                "_initialize"
            ))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
        }

        flags.addAll(buildList {
            add("-O2") // Optimize linked output for speed without the most expensive optimization passes.
            add("-fuse-ld=lld") // Use LLVM's lld linker.
            add("-flto") // Run link-time optimization across bitcode inputs.
            add("-Wl,--no-entry") // Do not require a _start entry point.
            if (isSideModule) {
                add("-shared") // Produce a shared/side WebAssembly module.
                add("-Wl,--export-all") // Export all module symbols for dynamic loading.
                add("-Wl,--import-memory") // Import linear memory from the host/main module.
                add("-Wl,--import-table") // Import the function table from the host/main module.
            } else {
                add("-Wl,--gc-sections") // Remove unused sections during linking.
                add("-Wl,--growable-table") // Allow side modules to reserve function table slots.
            }
            add("-Wl,--allow-undefined") // Allow unresolved symbols to become imports where possible.
            add("-mllvm") // Forward the next option directly to LLVM.
            add("-wasm-enable-sjlj") // Enable LLVM's WebAssembly setjmp/longjmp lowering pass.
            add("-mexception-handling") // Enable WASM EH support used by SjLj; C++ exceptions stay disabled.

            if (skiko.isWasmBuildWithProfiling) add("--profiling") // Keep function names for profiling output.
            addAll(resolvedBinaryInputs.linkFlags) // Link Skia and other resolved WASM binary inputs.
        })
        if (!isSideModule) {
            flags.addAll(exportsProvider.map { exports ->
                exports.map { "-Wl,--export=$it" } // Export symbols referenced by generated JS glue.
            })
        }

        doLast {
            // skiko.mjs is referenced in karma.config.d/*/config.js
            // so symbols must be replaced right after linking.
            // With WASI SDK (unlike Emscripten), the linker only produces a .wasm file,
            // so we must create the .mjs file ourselves from the callbacks + setup sources.
            val outputFileName = emccOutputFileName.get()
            if (!outputFileName.endsWith(".mjs")) {
                return@doLast
            }

            val emccOutputFile = outDir.asFile.get().resolve(outputFileName)
            val callbacks = project.layout.projectDirectory.file("src/webMain/resources/skikoCallbacks.js").asFile.readText()
            val setup = project.file(prefixPath).readText()
            emccOutputFile.writeText(callbacks + "\n" + setup)
        }
    }

    val linkWasm by project.tasks.registering(LinkSkikoWasmTask::class) {
        dependsOn(
            kotlin.wasmJs().compilations["main"].compileTaskProvider,
            kotlin.js().compilations["main"].compileTaskProvider
        )
        if (skiko.includeTestHelpers) {
            dependsOn(
                kotlin.wasmJs().compilations["test"].compileTaskProvider,
                kotlin.js().compilations["test"].compileTaskProvider
            )
        }

        buildSuffix.set("es6")
        emccOutputFileName.set(if (isSideModule) "$libBaseName.unoptimized.wasm" else "skiko.unoptimized.mjs") // this determines the name .wasm file too
        libOutputFileName.set("$libBaseName.unoptimized.wasm")
        val prefixPath = if (isSideModule) {
            project.sideModuleSetupMjs(libBaseName).normalize().absolutePath
        } else {
            project.setupMjs.normalize().absolutePath
        }
        configureCommon(prefixPath)
    }

    val linkWasmD8WithES6 by project.tasks.registering(LinkSkikoWasmTask::class) {
        dependsOn(
            kotlin.wasmJs().compilations["main"].compileTaskProvider,
            kotlin.js().compilations["main"].compileTaskProvider
        )
        if (skiko.includeTestHelpers) {
            dependsOn(
                kotlin.wasmJs().compilations["test"].compileTaskProvider,
                kotlin.js().compilations["test"].compileTaskProvider
            )
        }

        buildSuffix.set("d8")
        emccOutputFileName.set(if (isSideModule) "${libBaseName}d8.unoptimized.wasm" else "skikod8.unoptimized.mjs") // this determines the name .wasm file too
        libOutputFileName.set("${libBaseName}d8.unoptimized.wasm")

        val prefixPath = if (isSideModule) {
            project.sideModuleSetupMjs(libBaseName).normalize().absolutePath
        } else {
            project.setupMjs.normalize().absolutePath
        }
        configureCommon(prefixPath)
    }

    fun OptimizeSkikoWasmTask.configureCommonOptimize(
        linkTask: TaskProvider<LinkSkikoWasmTask>,
        nameSuffix: String = ""
    ) {
        dependsOn(setupEmscripten)
        buildTargetOS.set(OS.Wasm)
        buildTargetArch.set(Arch.Wasm)
        buildVariant.set(buildType)

        val wasmOptName = if (System.getProperty("os.name").startsWith("Win")) "wasm-opt.exe" else "wasm-opt"
        optimizer.set(emscriptenBinaryToolPath(wasmOptName))
        inputDir.set(linkTask.flatMap { it.outDir })
        libOutputFileName.set("$libBaseName$nameSuffix")

        flags.addAll(
            buildList {
                add("-Oz") // set optimization level to compress (highest size reduction)
                if (skiko.isWasmBuildWithProfiling) {
                    add("--debuginfo")
                } else {
                    // strip debug info (including the names section)
                    // only do so if we are not building with profiling, as names are required for profiling
                    add("--strip-debug")
                }
                add("--converge") // Run passes to convergence, continuing while binary size decreases
                add("--strip-producers") // strip the wasm producers section
                add("--enable-bulk-memory")
                add("--enable-exception-handling")
                add("--enable-nontrapping-float-to-int")
                add("--enable-sign-ext")
                add("--enable-threads")
            }
        )
    }

    val optimizeWasm by project.tasks.registering(OptimizeSkikoWasmTask::class) {
        dependsOn(linkWasm)
        buildSuffix.set("es6")
        configureCommonOptimize(linkWasm)
    }

    val optimizeWasmD8 by project.tasks.registering(OptimizeSkikoWasmTask::class) {
        dependsOn(linkWasmD8WithES6)
        buildSuffix.set("d8")
        configureCommonOptimize(linkWasmD8WithES6, "d8")
    }

    // skikoWasmJar is used by task name
    val skikoWasmJar by project.tasks.registering(Jar::class) {
        // We produce jar that contains .js of wrapper/bindings and .wasm with Skia + bindings.
        from(project.setupReexportMjs(libBaseName).parentFile) {
            include(project.setupReexportMjs(libBaseName).name)

            if (isSideModule) {
                include(project.sideModuleSetupMjs(libBaseName).name)
            }
        }

        from(optimizeWasm) {
            include("$libBaseName.wasm")
            include("*.mjs")
        }

        from(optimizeWasmD8) {
            include("*.mjs")
            filesMatching("*.mjs") {
                filter { it.replace("${libBaseName}d8.wasm", "$libBaseName.wasm") }
            }
        }

        archiveBaseName.set("skiko-wasm")
        doLast {
            println("Wasm and JS at: ${archiveFile.get().asFile.absolutePath}")
        }
    }
}

private fun SetupEmscriptenTask.nodeExecutableFile(): File {
    val executableName = if (hostOs.isWindows) "node.exe" else "node"
    val nodeRoot = sdkDir.get().asFile.resolve("node")
    val candidates = nodeRoot
        .takeIf { it.isDirectory }
        ?.walkTopDown()
        ?.filter { it.isFile && it.name == executableName }
        ?.toList()
        .orEmpty()

    return candidates.firstOrNull { it.parentFile.name == "bin" } ?: candidates.firstOrNull()
        ?: throw GradleException("Could not find Emscripten node executable under ${nodeRoot.absolutePath}")
}

fun SkikoProjectContext.provideWasmSideModules() {
    provideWasmSideModule(mainLinkTaskName = "linkWasm")
    provideWasmSideModule(mainLinkTaskName = "linkWasmD8WithES6")
}

fun SkikoProjectContext.provideWasmTestResources() = with(project) {
    val optimizeWasm = tasks.named<OptimizeSkikoWasmTask>("optimizeWasm")
    val wasmTestRuntimeResources = tasks.register<Sync>("wasmTestRuntimeResources") {
        dependsOn(optimizeWasm)
        from(optimizeWasm.flatMap { it.outDir }) {
            include("$libBaseName.wasm")
            include("*.mjs")
        }
        into(layout.buildDirectory.dir("wasmTestRuntimeResources"))
    }
    configurations.create("wasmTestResourcesElements") {
        isCanBeConsumed = true
        isCanBeResolved = false

        attributes {
            attribute(
                Usage.USAGE_ATTRIBUTE,
                objects.named(Usage::class.java, WASM_TEST_RESOURCES_USAGE)
            )
        }

        outgoing.artifact(wasmTestRuntimeResources.map { it.destinationDir })
        outgoing.artifact(wasmImports) {
            builtBy(
                optimizeWasm,
                tasks.named("compileTestKotlinJs"),
                tasks.named("compileTestKotlinWasmJs"),
            )
        }
    }
}

private fun SkikoProjectContext.provideWasmSideModule(mainLinkTaskName: String) = with(project) {
    val sideLinkTask = tasks.named<LinkSkikoWasmTask>(mainLinkTaskName)
    configurations.create("wasmSideModuleElements${mainLinkTaskName.replaceFirstChar { it.titlecase() }}") {
        isCanBeConsumed = true
        isCanBeResolved = false

        attributes {
            attribute(wasmSideModuleLinkTaskAttribute, mainLinkTaskName)
            attribute(
                Usage.USAGE_ATTRIBUTE,
                objects.named(Usage::class.java, WASM_SIDE_MODULE_USAGE)
            )
        }

        outgoing.artifact(sideLinkTask.flatMap { task ->
            task.outDir.file(task.libOutputFileName)
        }) {
            builtBy(sideLinkTask)
        }
    }
}

fun SkikoProjectContext.configureWasmMainModuleSideModuleInputs(
    linkWasmSideModules: Configuration,
    linkWasmD8SideModules: Configuration,
) {
    configureSideModuleInput(
        mainLinkTaskName = "linkWasm",
        sideModuleFiles = project.files(linkWasmSideModules)
    )
    configureSideModuleInput(
        mainLinkTaskName = "linkWasmD8WithES6",
        sideModuleFiles = project.files(linkWasmD8SideModules)
    )
}

fun SkikoProjectContext.wasmSideModulesFor(
    mainLinkTaskName: String,
): Configuration = with(project) {
    configurations.create("wasmSideModules${mainLinkTaskName.replaceFirstChar { it.titlecase() }}") {
        isCanBeConsumed = false
        isCanBeResolved = true

        attributes {
            attribute(wasmSideModuleLinkTaskAttribute, mainLinkTaskName)
            attribute(
                Usage.USAGE_ATTRIBUTE,
                objects.named(Usage::class.java, WASM_SIDE_MODULE_USAGE)
            )
        }
    }
}

fun SkikoProjectContext.wasmTestResourcesFor(): Configuration = with(project) {
    configurations.create("wasmTestResources") {
        isCanBeConsumed = false
        isCanBeResolved = true

        attributes {
            attribute(
                Usage.USAGE_ATTRIBUTE,
                objects.named(Usage::class.java, WASM_TEST_RESOURCES_USAGE)
            )
        }
    }
}

private fun SkikoProjectContext.configureSideModuleInput(
    mainLinkTaskName: String,
    sideModuleFiles: ConfigurableFileCollection
) {
    val setupEmscripten = project.tasks.named<SetupEmscriptenTask>("setupEmscripten")
    // Generate a narrow export list from the side modules before linking the main
    // module; otherwise unresolved side-module imports would fail at runtime.
    val sideModuleExports = project.tasks.register<GenerateWasmSideModuleExportsTask>(
        "generateWasmSideModuleExports${mainLinkTaskName.replaceFirstChar { it.titlecase() }}"
    ) {
        dependsOn(setupEmscripten)
        dependsOn(sideModuleFiles)
        nodeExecutable.set(project.layout.file(setupEmscripten.map { it.nodeExecutableFile() }))
        sideModules.from(sideModuleFiles)
        outputFile.set(project.wasmSideModuleExportSymbols(mainLinkTaskName))
    }

    // Side modules built with -shared are dynamic wasm modules loaded at runtime.
    // They must NOT be passed to the core linker (wasm-ld cannot statically link
    // a dynamic object). Instead, copy them into the core link output directory
    // so they are bundled alongside the core .wasm for runtime loading.
    project.tasks.named<LinkSkikoWasmTask>(mainLinkTaskName).configure {
        dependsOn(sideModuleExports)
        inputs.file(sideModuleExports.flatMap { it.outputFile })
            .withPathSensitivity(PathSensitivity.RELATIVE)
        doLast {
            sideModuleFiles.files.forEach { sideWasm ->
                sideWasm.copyTo(outDir.get().asFile.resolve(sideWasm.name), overwrite = true)
            }
        }
    }
}

abstract class AbstractImportGeneratorCompilerPluginSupportPlugin(
    val compilationName: String,
    private val outputFileProvider: (Project) -> File,
    private val prefixFileProvider: (Project) -> File,
    private val reexportFileProvider: ((Project) -> File)?,
    private val exportsFileProvider: ((Project) -> File)?,
    private val moduleNameProvider: (Project) -> String
) : KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        val outputFile = outputFileProvider(project)
        val prefixFile = prefixFileProvider(project)
        val reexportFile = reexportFileProvider?.invoke(project)
        val exportsFile = exportsFileProvider?.invoke(project)
        val moduleName = moduleNameProvider(project)

        return project.provider {
            buildList {
                add(SubpluginOption("import-generator-path", outputFile.normalize().absolutePath))
                add(SubpluginOption("import-generator-prefix", prefixFile.normalize().absolutePath))
                if (reexportFile != null) {
                    add(SubpluginOption("import-generator-reexport-path", reexportFile.normalize().absolutePath))
                }
                if (exportsFile != null) {
                    add(SubpluginOption("import-generator-exports-path", exportsFile.normalize().absolutePath))
                }
                add(SubpluginOption("import-generator-module-name", moduleName))
            }
        }
    }

    override fun getCompilerPluginId() = "org.jetbrains.skiko.imports.generator"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(SkikoArtifacts.DEFAULT_GROUP_ID, IMPORT_GENERATOR)

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return ((kotlinCompilation.platformType == KotlinPlatformType.wasm) || (kotlinCompilation.platformType == KotlinPlatformType.js))
                && kotlinCompilation.name == compilationName
    }
}

class WasmImportsGeneratorCompilerPluginSupportPlugin : AbstractImportGeneratorCompilerPluginSupportPlugin(
    KotlinCompilation.MAIN_COMPILATION_NAME,
    { it.setupMjs },
    { it.generatedPreSetupMjs },
    { it.setupReexportMjs(it.name) },
    { it.wasmExportSymbols },
    { it.name }
)

class WasmImportsGeneratorForTestCompilerPluginSupportPlugin : AbstractImportGeneratorCompilerPluginSupportPlugin(
    KotlinCompilation.TEST_COMPILATION_NAME,
    { it.skikoTestMjs(it.name) },
    {
        val preludeFileName = "pre-${it.name}-test.mjs"
        it.projectDir.resolve("src/webMain/resources/$preludeFileName")
    },
    null,
    { it.wasmTestExportSymbols },
    { it.name }
)

class SideWasmImportsGeneratorPlugin : AbstractImportGeneratorCompilerPluginSupportPlugin(
    KotlinCompilation.MAIN_COMPILATION_NAME,
    { it.sideModuleSetupMjs(it.name) },
    { it.projectDir.resolve("src/webMain/resources/pre-${it.name}.mjs") },
    { it.setupReexportMjs(it.name) },
    null,
    { it.name }
)

fun KotlinJsTargetDsl.setupImportsGeneratorPlugin(
    libBaseName: String,
    isSideModule: Boolean
) {
    val main by compilations.getting
    val test by compilations.getting
    val mainPrefixFile = if (isSideModule) {
        project.projectDir.resolve("src/webMain/resources/pre-$libBaseName.mjs")
    } else {
        project.generatedPreSetupMjs
    }
    val testPrefixFile = project.projectDir.resolve("src/webMain/resources/pre-${project.name}-test.mjs")

    main.compileTaskProvider.configure {
        outputs.file(if (isSideModule) project.sideModuleSetupMjs(libBaseName) else project.setupMjs)
        inputs.file(mainPrefixFile)
            .withPathSensitivity(PathSensitivity.RELATIVE)
        if (!isSideModule) {
            outputs.file(project.wasmExportSymbols)
        }
    }

    test.compileTaskProvider.configure {
        outputs.file(project.skikoTestMjs(libBaseName))
        outputs.file(project.wasmTestExportSymbols)
        if (testPrefixFile.exists()) {
            inputs.file(testPrefixFile)
                .withPathSensitivity(PathSensitivity.RELATIVE)
        }
    }

    listOf(main, test).forEach {
        // By default, it will try to use the same version as kotlin, because we use version=null in getPluginArtifact.
        // But we don't publish the artifact, therefore we substitute it for project dependency.
        it.configurations.pluginConfiguration.resolutionStrategy.dependencySubstitution {
            substitute(module("${SkikoArtifacts.DEFAULT_GROUP_ID}:$IMPORT_GENERATOR"))
                .using(project(":import-generator"))
        }
    }
}
