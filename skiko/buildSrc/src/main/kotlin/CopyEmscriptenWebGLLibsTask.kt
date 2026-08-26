import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

/**
 * Copies Emscripten's WebGL library JS into generated build output after
 * running Emscripten's own preprocessor/macro expansion.
 */
abstract class CopyEmscriptenWebGLLibsTask : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val nodeExecutable: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val emscriptenLibDir: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val preprocessor: RegularFileProperty

    @get:Input
    abstract val settingsJson: Property<String>

    @get:Input
    abstract val libFiles: ListProperty<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val prefixFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val localImportFiles: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        settingsJson.convention("""{"MIN_WEBGL_VERSION":2,"MAX_WEBGL_VERSION":2}""")
    }

    @TaskAction
    fun run() {
        // import-generator resolves and inlines local imports relative to the prefix file.
        // Copy the prefix and its local imports next to the generated Emscripten libs so
        // `import "./libwebgl*.preprocessed.js"` can be resolved without checking in those files.
        outputDir.get().asFile.deleteRecursively()
        copyToOutputDir(prefixFile.get().asFile)
        localImportFiles.files.forEach(::copyToOutputDir)
        libFiles.get().forEach { libFile ->
            preprocess(
                source = sourceFile(libFile),
                output = outputFile(libFile)
            )
        }
    }

    private fun preprocess(source: File, output: File) {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execOperations.exec {
            commandLine(
                nodeExecutable.get().asFile.absolutePath,
                preprocessor.get().asFile.absolutePath,
                "-",
                source.name,
                "--expand-macros"
            )
            workingDir = source.parentFile
            // Emscripten's preprocessor reads settings JSON from stdin and writes the
            // expanded library source to stdout; it does not write an output file itself.
            standardInput = ByteArrayInputStream(settingsJson.get().toByteArray())
            standardOutput = stdout
            errorOutput = stderr
            isIgnoreExitValue = true
        }

        val stdoutText = stdout.toString()
        val stderrText = stderr.toString().trim()
        if (stderrText.isNotEmpty()) {
            logger.warn(stderrText)
        }
        if (result.exitValue != 0) {
            throw GradleException(
                "Failed to preprocess ${source.absolutePath} with Emscripten " +
                    "(exit code ${result.exitValue}).\nstderr: $stderrText"
            )
        }

        output.parentFile.mkdirs()
        // Keep the generated filename stable for the static imports in pre-setup.mjs.
        output.writeText(stdoutText)
        logger.lifecycle("Copied preprocessed ${source.name} to ${output.absolutePath}")
    }

    private fun copyToOutputDir(source: File) {
        val output = outputDir.file(source.name).get().asFile
        output.parentFile.mkdirs()
        source.copyTo(output, overwrite = true)
    }

    private fun sourceFile(libFile: String): File =
        emscriptenLibDir.file(libFile).get().asFile

    private fun outputFile(libFile: String): File =
        outputDir.file("${File(libFile).nameWithoutExtension}.preprocessed.js").get().asFile
}
