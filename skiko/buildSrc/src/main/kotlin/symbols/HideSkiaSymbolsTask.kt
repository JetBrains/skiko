package symbols

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
import javax.inject.Inject
import OS

abstract class HideSkiaSymbolsTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    abstract val targetOs: Property<OS>

    @get:Input
    abstract val symbolExtractorCommand: ListProperty<String>

    @get:InputFiles
    abstract val symbolSourceLibraries: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun write() {
        val os = targetOs.get()
        val symbols = SymbolExtractor(
            execOperations = execOperations,
            os = os,
            command = symbolExtractorCommand.get(),
        ).extract(symbolSourceLibraries.files, SymbolType.DefinedGlobal)
            .filter { !isOrgJetbrainsSymbol(it) }
            .sorted()

        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(if (os.isLinux) versionScript(local = symbols) else symbols.joinToString("\n", postfix = "\n"))
    }
}
