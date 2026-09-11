import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

/**
 * Copies Emscripten's WebGL library JS into generated build output after
 * running Emscripten's own preprocessor/macro expansion.
 */
abstract class GenerateEmscriptenWebGLPrefixTask : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val nodeExecutable: RegularFileProperty

    @get:InputFile
    abstract val compatibilityFile: RegularFileProperty

    @get:InputFile
    abstract val setupBodyFile: RegularFileProperty


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

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        settingsJson.convention("""{"MIN_WEBGL_VERSION":2,"MAX_WEBGL_VERSION":2}""")
    }

    @TaskAction
    fun run() {
        val fragments = buildList {
            add(compatibilityFile.get().asFile.readText())

            libFiles.get().forEach { fileName ->
                add(preprocess(sourceFile(fileName)))
            }

            add(setupBodyFile.get().asFile.readText())
        }

        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(fragments.joinToString(separator = "\n\n"))
    }

    private fun preprocess(source: File): String {
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

        if (result.exitValue != 0) {
            throw GradleException(
                "Failed to preprocess ${source.absolutePath} with Emscripten " +
                        "(exit code ${result.exitValue}).\nstderr: $stderr"
            )
        }

        return stdout.toString()
    }

    private fun sourceFile(libFile: String): File =
        emscriptenLibDir.file(libFile).get().asFile

}
