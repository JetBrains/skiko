package tasks.configuration

import Arch
import CompileSkikoCppTask
import LinkSkikoWasmTask
import OS
import SkikoProjectContext
import compilerForTarget
import linkerForTarget
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import projectDirs
import registerOrGetSkiaDirProvider
import supportJs
import supportWasm

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
            listOf(
                *skiaPreprocessorFlags(OS.Wasm, buildType),
                *buildType.clangFlags,
                "-fno-rtti",
                "-fno-exceptions",
            )
        )
    }

    val configureCommon: LinkSkikoWasmTask.(outputES6: Boolean) -> Unit = { outputES6 ->
        val osArch = OS.Wasm to Arch.Wasm

        dependsOn(compileWasm)
        dependsOn(skiaWasmDir)
        val unpackedSkia = skiaWasmDir.get()

        linker.set(linkerForTarget(OS.Wasm, Arch.Wasm))
        buildTargetOS.set(osArch.first)
        buildTargetArch.set(osArch.second)
        buildVariant.set(buildType)
        if (outputES6) buildSuffix.set("es6")

        libFiles = project.fileTree(unpackedSkia) { include("**/*.a") }
        objectFiles = project.fileTree(compileWasm.map { it.outDir.get() }) {
            include("**/*.o")
        }

        val jsFileExtension = if (outputES6) "mjs" else "js"
        val wasmFileName = if (outputES6) {
            "skikomjs.wasm"
        } else {
            "skiko.wasm" // to keep it compatible with older apps
        }
        val jsFileName = if (outputES6) {
            "skikomjs.mjs"
        } else {
            "skiko.js" // to keep it compatible with older apps
        }
        libOutputFileName.set(wasmFileName) // emcc ignores this, it names .wasm file identically to js output
        jsOutputFileName.set(jsFileName) // this determines the name .wasm file too

        skikoJsPrefix.from(
            // the order matters
            project.layout.projectDirectory.file("src/jsWasmMain/resources/skikoCallbacks.js"),
            project.layout.projectDirectory.file("src/jsWasmMain/resources/setup.$jsFileExtension")
        )

        @OptIn(kotlin.ExperimentalStdlibApi::class)
        flags.set(buildList {
            addAll(
                listOf(
                    "-l", "GL",
                    "-s", "MAX_WEBGL_VERSION=2",
                    "-s", "MIN_WEBGL_VERSION=2",
                    "-s", "OFFSCREEN_FRAMEBUFFER=1",
                    "-s", "ALLOW_MEMORY_GROWTH=1", // TODO: Is there a better way? Should we use `-s INITIAL_MEMORY=X`?
                    "--bind",
                    // -O2 saves 800kB for the output file, and ~100kB for transferred size.
                    // -O3 breaks the exports in js/mjs files. skiko.wasm size is the same though
                    "-O2"
                )
            )
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
            }
        })

        doLast {
            // skiko.js (and skiko.mjs) files are directly referenced in karma.config.d/*/config.js
            // so symbols must be replaced right after linking
            val jsFiles = outDir.asFile.get().walk()
                .filter { it.isFile && (it.name.endsWith(".js") || it.name.endsWith(".mjs")) }

            for (jsFile in jsFiles) {
                val originalContent = jsFile.readText()
                val newContent = originalContent.replace("_org_jetbrains", "org_jetbrains")
                    .replace("skikomjs.wasm", "skiko.wasm")
                    .replace("if(ENVIRONMENT_IS_NODE){", "if (false) {") // to make webpack erase this part
                jsFile.writeText(newContent)

                if (outputES6) {
                    // delete this file as its presence can be confusing.
                    // It's identical to skiko.wasm and we use skiko.wasm in `skikoWasmJar`task
                    outDir.file(wasmFileName).get().asFile.delete()

                    outDir.file(jsFileName).get().asFile.renameTo(outDir.asFile.get().resolve("skiko.mjs"))
                }
            }
        }
    }

    val linkWasmWithES6 by tasks.registering(LinkSkikoWasmTask::class) {
        configureCommon(true)
    }

    val linkWasm by tasks.registering(LinkSkikoWasmTask::class) {
        dependsOn(linkWasmWithES6)
        configureCommon(false)
    }

    // skikoWasmJar is used by task name
    val skikoWasmJar by project.tasks.registering(Jar::class) {
        dependsOn(linkWasm)
        // We produce jar that contains .js of wrapper/bindings and .wasm with Skia + bindings.
        val wasmOutDir = linkWasm.map { it.outDir }
        val wasmEsOutDir = linkWasmWithES6.map { it.outDir }

        from(wasmOutDir) {
            include("*.wasm")
            include("*.js")
            include("*.mjs")
        }
        from(wasmEsOutDir) {
            include("*.mjs")
        }

        archiveBaseName.set("skiko-wasm")
        doLast {
            println("Wasm and JS at: ${archiveFile.get().asFile.absolutePath}")
        }
    }

    return LinkWasmTasks(linkWasm, linkWasmWithES6)
}