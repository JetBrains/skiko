import internal.utils.*

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

abstract class LinkSkikoWasmTask : LinkSkikoTask() {
    @get:InputFile
    @get:Optional
    abstract val skikoJsPrefix: RegularFileProperty

    @get:Input
    abstract val jsOutputFileName: Property<String>

    override fun configureArgs() =
        super.configureArgs().apply {
            arg("-o", outDir.resolveToIoFile(jsOutputFileName))
            arg("--extern-post-js", skikoJsPrefix)
        }
}