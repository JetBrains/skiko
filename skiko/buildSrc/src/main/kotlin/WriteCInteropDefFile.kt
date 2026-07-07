import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class WriteCInteropDefFile : DefaultTask() {
    @get:Input
    abstract val headers: ListProperty<String>

    @get:Input
    abstract val compilerOpts: ListProperty<String>

    @get:Input
    abstract val linkerOpts: ListProperty<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun run() {
        val outputFile = outputFile.get().asFile
        outputFile.parentFile.mkdirs()

        outputFile.bufferedWriter().use { writer ->
            val headers = headers.get()
            if (headers.isNotEmpty()) {
                writer.appendLine("headers=${headers.joinToString(" ")}")
            }
            val compilerOpts = compilerOpts.get()
            if (compilerOpts.isNotEmpty()) {
                writer.appendLine("compilerOpts=${compilerOpts.joinToString(" ")}")
            }
            val linkerOpts = linkerOpts.get()
            if (linkerOpts.isNotEmpty()) {
                writer.appendLine("linkerOpts=${linkerOpts.joinToString(" ")}")
            }
        }
    }
}
