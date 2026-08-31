package org.jetbrains.skiko.build.wasm

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.jetbrains.skiko.build.cpp.LinkSkikoTask
import org.jetbrains.skiko.build.utils.*

abstract class LinkSkikoWasmTask : LinkSkikoTask() {
    @get:InputFiles
    @get:Optional
    abstract val externPostJs: ConfigurableFileCollection

    @get:Input
    abstract val emccOutputFileName: Property<String>

    override fun configureArgs() =
        super.configureArgs().apply {
            arg("-o", outDir.resolveToIoFile(emccOutputFileName))
            // https://emscripten.org/docs/tools_reference/emcc.html
            repeatedArg("--extern-post-js", externPostJs.files)
        }
}
