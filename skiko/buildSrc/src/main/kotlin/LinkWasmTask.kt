import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class LinkWasmTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFiles
    lateinit var libFiles: FileCollection

    @get:InputFiles
    lateinit var objectFiles: FileCollection

    @get:Input
    abstract val wasmFileName: Property<String>

    @get:Input
    abstract val jsFileName: Property<String>

    @get:InputFile
    @get:Optional
    abstract val skikoJsPrefix: RegularFileProperty

    @get:OutputDirectory
    abstract val outDir: DirectoryProperty

    @get:Input
    abstract val flags: ListProperty<String>

    @TaskAction
    fun run() {
        outDir.get().asFile.apply {
            deleteRecursively()
            mkdirs()
        }

        execOperations.exec {
            executable = "emcc"
            args = arrayListOf<String>().also { configureCompilerArgs(it) }

            workingDir = outDir.get().asFile
            // todo: log args to file system
        }
    }

    fun configureCompilerArgs(args: MutableList<String>) {
        args.addAll(flags.get())
        args.addAll(objectFiles.files.map { it.absolutePath })
        args.addAll(libFiles.files.map { it.absolutePath })
        args.add("--extern-post-js")
        args.add(skikoJsPrefix.get().asFile.absolutePath)
        args.add("-o")
        args.add(outDir.resolveToAbsolutePath(wasmFileName))
        args.add("-o")
        args.add(outDir.resolveToAbsolutePath(jsFileName))
    }
}