import internal.utils.*
import org.gradle.api.file.ConfigurableFileCollection

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional

abstract class LinkSkikoWasmTask : LinkSkikoTask() {
    @get:InputFiles
    @get:Optional
    abstract val externPostJs: ConfigurableFileCollection

    @get:Input
    abstract val jsOutputFileName: Property<String>

    override fun configureArgs(): ArgBuilder =
        // NOTE: `emcc` should be invoked with a single `-o` output.
        // Passing `-o` twice (first for `.wasm`, then for `.mjs`) makes Emscripten treat the link as a
        // standalone Wasm build in some toolchain versions, which breaks JS-runtime-provided symbols.
        createArgBuilder().apply {
            arg("-o", outDir.resolveToIoFile(jsOutputFileName))

            repeatedArg("-L", values = libDirs.get())
            repeatedArg(values = objectFiles.files)
            repeatedArg(values = libFiles.files)
            rawArgs(flags.get())

            // https://emscripten.org/docs/tools_reference/emcc.html
            repeatedArg("--extern-post-js", externPostJs.files)
        }
}
