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
    abstract val emccOutputFileName: Property<String>

    override fun configureArgs() =
        super.configureArgs()
}