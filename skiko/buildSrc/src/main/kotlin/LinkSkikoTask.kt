import internal.utils.*

import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

abstract class LinkSkikoTask : AbstractSkikoNativeToolTask() {
    @get:InputFiles
    lateinit var libFiles: FileCollection

    @get:Input
    abstract val libDirs: ListProperty<File>

    @get:InputFiles
    lateinit var objectFiles: FileCollection

    @get:Input
    abstract val libOutputFileName: Property<String>

    @get:Input
    abstract val flags: ListProperty<String>

    @get:Input
    abstract val linker: Property<String>

    override val outDirNameForTool: String
        get() = "link"

    private val argsFile = taskStateDir.file("args.txt")

    override fun createArgBuilder(): ArgBuilder =
        if (buildTargetOS.get().isWindows) VisualCppLinkerArgBuilder()
        else super.createArgBuilder()

    override fun execute(mode: ToolMode, args: ArgBuilder) {
        check(mode is ToolMode.NonIncremental) {
            "Linking is not incremental, but $mode is received"
        }

        val argFile = argsFile.get().asFile
        val argFileArg = args.createArgFile(argFile)
        logArgs("Linker args", args, argFile)

        execOperations.exec {
            executable = findLinkerExecutable()
            workingDir = outDir.get().asFile
            this.args = listOf(argFileArg)
        }
    }

    private fun findLinkerExecutable(): String {
        val linkerName = linker.get()
        val linkerFile = File(linkerName)
        if (linkerFile.isFile) return linkerFile.absolutePath

        val paths = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
        for (path in paths) {
            val file = File(path).resolve(linkerName)
            if (file.isFile) return file.absolutePath
        }

        // Check locally installed emsdk (installed by EnsureEmscriptenTask)
        val localEmsdkLinker = project.layout.buildDirectory.dir("emsdk/upstream/emscripten").get().asFile.resolve(linkerName)
        if (localEmsdkLinker.isFile) return localEmsdkLinker.absolutePath

        return linkerName
    }

    override fun configureArgs() =
        super.configureArgs().apply {
            arg("-o", outDir.resolveToIoFile(libOutputFileName))

            repeatedArg("-L", values = libDirs.get())
            repeatedArg(values = objectFiles.files)
            repeatedArg(values = libFiles.files)
            rawArgs(flags.get())
        }
}