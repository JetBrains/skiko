package symbols

import OS
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class GenerateRequiredSymbolsTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    abstract val targetOs: Property<OS>

    @get:Input
    abstract val symbolExtractorCommand: ListProperty<String>

    @get:InputFiles
    abstract val objectFiles: ConfigurableFileCollection

    @get:InputFiles
    abstract val libs: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val files = objectFiles.files.toList() + libs.files.toList()
        val symbols = extractUndefinedSymbols(files).distinct().sorted()
        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(symbols.joinToString("\n", postfix = if (symbols.isEmpty()) "" else "\n"))
        }
        logger.lifecycle("Required symbols: ${symbols.size}")
    }

    private fun extractUndefinedSymbols(files: List<File>): List<String> {
        return SymbolExtractor(
            execOperations = execOperations,
            os = targetOs.get(),
            command = symbolExtractorCommand.get(),
        ).extract(files, SymbolType.Undefined).toList()
    }
}
