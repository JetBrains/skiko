import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class WriteCInteropDefFile : DefaultTask() {
    @get:Input
    abstract val linkerOpts: ListProperty<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun run() {
        val outputFile = outputFile.get().asFile
        outputFile.parentFile.mkdirs()

        outputFile.bufferedWriter().use { writer ->
            val linkerOpts = linkerOpts.get()
            if (linkerOpts.isNotEmpty()) {
                writer.appendLine("linkerOpts=${linkerOpts.joinToString(" ")}")
            }
        }
    }
}