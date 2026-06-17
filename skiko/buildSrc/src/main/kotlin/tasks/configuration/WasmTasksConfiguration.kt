package tasks.configuration

import Arch
import CompileSkikoCppTask
import IMPORT_GENERATOR
import LinkSkikoWasmTask
import OS
import SkikoModuleKind
import SkikoProjectContext
import compilerForTarget
import dsl.TargetEnv
import linkerForTarget
import org.gradle.api.artifacts.Configuration
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import projectDirs
import registerOrGetSkiaDirProvider
import supportWeb
import wasmImports
import wasmImport
import java.io.File

private val Project.setupMjs
    get() = wasmImport("setup.mjs")

private fun Project.sideModuleSetupMjs(libBaseName: String) =
    wasmImport("$libBaseName.mjs")

private fun Project.setupReexportMjs(libBaseName: String) =
    wasmImport("js-$libBaseName-reexport-symbols.mjs")

private fun Project.skikoTestMjs(libBaseName: String) =
    wasmImport("$libBaseName-test.mjs")

private val wasmSideModuleLinkTaskAttribute =
    Attribute.of("org.jetbrains.skiko.wasmSideModule.linkTask", String::class.java)

private const val WASM_SIDE_MODULE_USAGE = "skiko-wasm-side-module"
private const val WASM_TEST_RESOURCES_USAGE = "skiko-wasm-test-resources"

fun SkikoProjectContext.declareWasmTasks() {
    if (!project.supportWeb) {
        return
    }
    val isSideModule = kind == SkikoModuleKind.EXTENSION

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
        if (isSideModule) {
            val coreProjectDir = project.rootProject.projectDir
            includeHeadersNonRecursive(coreProjectDir.resolve("src/nativeJsMain/cpp"))
            includeHeadersNonRecursive(coreProjectDir.resolve("src/commonMain/cpp/common/include"))
        }
        includeHeadersNonRecursive(skiaHeadersDirs(skiaWasmDir.get()))

        flags.set(
            buildList {
                addAll(skiaPreprocessorFlags(OS.Wasm, buildType))
                addAll(buildType.clangFlags)
                add("-fno-rtti")
                add("-fno-exceptions")
                add("-fPIC")
                if (skiko.isWasmBuildWithProfiling) add("--profiling")
            }
        )
    }

    fun LinkSkikoWasmTask.configureCommon(prefixPath: String) {
        dependsOn(compileWasm)
        dependsOn(skiaWasmDir)
        val skiaBinDir = skiaWasmDir.get().resolve("out/${buildType.id}-wasm-wasm").absolutePath
        val resolvedBinaryInputs = resolveBinaryInputs(OS.Wasm, Arch.Wasm, TargetEnv.WASM, skiaBinDir)

        linker.set(linkerForTarget(OS.Wasm, Arch.Wasm))
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

        flags.addAll(buildList {
            addAll(
                listOf(
                    "-s", "OFFSCREEN_FRAMEBUFFER=1",
                    "-s", "ALLOW_MEMORY_GROWTH=1", // TODO: Is there a better way? Should we use `-s INITIAL_MEMORY=X`?
                    "-s", "SUPPORT_LONGJMP=wasm",
                    // -O2 saves 800kB for the output file, and ~100kB for transferred size.
                    // -O3 breaks the exports in js/mjs files. skiko.wasm size is the same though
                    "-O2"
                )
            )

            if (skiko.isWasmBuildWithProfiling) add("--profiling")
            addAll(resolvedBinaryInputs.linkFlags)
        })

        doLast {
            // skiko.mjs is referenced in karma.config.d/*/config.js
            // so symbols must be replaced right after linking
            val outputFileName = emccOutputFileName.get()
            if (!outputFileName.endsWith(".mjs")) {
                return@doLast
            }

            val emccOutputFile = outDir.asFile.get().walk().first { it.name == outputFileName }

            val isEnvironmentNodeCheckRegex = Regex(
                // spacing is different in release and debug builds
                """if\s*\(ENVIRONMENT_IS_NODE\)\s*\{"""
            )

            val originalContent = emccOutputFile.readText()
            val newContent = originalContent
                .replace(isEnvironmentNodeCheckRegex, "if (false) {") // to make webpack erase this part
            emccOutputFile.writeText(newContent)
        }
    }

    val linkWasm by project.tasks.registering(LinkSkikoWasmTask::class) {
        dependsOn(
            kotlin.wasmJs().compilations["main"].compileTaskProvider,
            kotlin.js().compilations["main"].compileTaskProvider
        )

        buildSuffix.set("es6")
        emccOutputFileName.set(if (isSideModule) "$libBaseName.wasm" else "skiko.mjs") // this determines the name .wasm file too
        libOutputFileName.set("$libBaseName.wasm")
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

        buildSuffix.set("d8")
        emccOutputFileName.set(if (isSideModule) "${libBaseName}d8.wasm" else "skikod8.mjs") // this determines the name .wasm file too
        libOutputFileName.set("${libBaseName}d8.wasm")

        flags.addAll(listOf("-s", "ENVIRONMENT=shell"))

        val prefixPath = if (isSideModule) {
            project.sideModuleSetupMjs(libBaseName).normalize().absolutePath
        } else {
            project.setupMjs.normalize().absolutePath
        }
        configureCommon(prefixPath)
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

        from(linkWasm) {
            include("*.wasm")
            include("*.mjs")
        }

        from(linkWasmD8WithES6) {
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

fun SkikoProjectContext.provideWasmSideModules() {
    provideWasmSideModule(mainLinkTaskName = "linkWasm")
    provideWasmSideModule(mainLinkTaskName = "linkWasmD8WithES6")
}

fun SkikoProjectContext.provideWasmTestResources() = with(project) {
    val linkWasm = tasks.named<LinkSkikoWasmTask>("linkWasm")
    configurations.create("wasmTestResourcesElements") {
        isCanBeConsumed = true
        isCanBeResolved = false

        attributes {
            attribute(
                Usage.USAGE_ATTRIBUTE,
                objects.named(Usage::class.java, WASM_TEST_RESOURCES_USAGE)
            )
        }

        outgoing.artifact(linkWasm.flatMap { it.outDir })
        outgoing.artifact(wasmImports) {
            builtBy(
                linkWasm,
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
        })
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
    project.tasks.named<LinkSkikoWasmTask>(mainLinkTaskName).configure {
        libFiles += sideModuleFiles
    }
}

abstract class AbstractImportGeneratorCompilerPluginSupportPlugin(
    val compilationName: String,
    private val outputFileProvider: (Project) -> File,
    private val prefixFileProvider: (Project) -> File,
    private val reexportFileProvider: ((Project) -> File)?,
    private val moduleNameProvider: (Project) -> String
) : KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        val outputFile = outputFileProvider(project)
        val prefixFile = prefixFileProvider(project)
        val reexportFile = reexportFileProvider?.invoke(project)
        val moduleName = moduleNameProvider(project)

        return project.provider {
            buildList {
                add(SubpluginOption("import-generator-path", outputFile.normalize().absolutePath))
                add(SubpluginOption("import-generator-prefix", prefixFile.normalize().absolutePath),)
                if (reexportFile != null) {
                    add(SubpluginOption("import-generator-reexport-path", reexportFile.normalize().absolutePath))
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
    { it.projectDir.resolve("src/webMain/resources/pre-setup.mjs") },
    { it.setupReexportMjs(it.name) },
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
    { it.name }
)

class SideWasmImportsGeneratorPlugin : AbstractImportGeneratorCompilerPluginSupportPlugin(
    KotlinCompilation.MAIN_COMPILATION_NAME,
    { it.sideModuleSetupMjs(it.name) },
    { it.projectDir.resolve("src/webMain/resources/pre-${it.name}.mjs") },
    { it.setupReexportMjs(it.name) },
    { it.name }
)

fun KotlinJsTargetDsl.setupImportsGeneratorPlugin(
    libBaseName: String,
    isSideModule: Boolean
) {
    val main by compilations.getting
    val test by compilations.getting

    main.compileTaskProvider.configure {
        outputs.file(if (isSideModule) project.sideModuleSetupMjs(libBaseName) else project.setupMjs)
    }

    test.compileTaskProvider.configure {
        outputs.file(project.skikoTestMjs(libBaseName))
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
