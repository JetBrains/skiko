import internal.utils.*

import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

abstract class ArchiveSkikoTask : AbstractSkikoNativeToolTask() {
    @get:InputFiles
    lateinit var objectFiles: FileCollection

    @get:Input
    abstract val libOutputFileName: Property<String>

    @get:Input
    abstract val flags: ListProperty<String>

    @get:Input
    abstract val archiver: Property<String>

    override val outDirNameForTool: String
        get() = "lib"

    private val argsFile = taskStateDir.file("args.txt")

    override fun createArgBuilder(): ArgBuilder =
        if (buildTargetOS.get().isWindows) VisualCppArchiverArgBuilder()
        else super.createArgBuilder()

    override fun execute(mode: ToolMode, args: ArgBuilder) {
        check(mode is ToolMode.NonIncremental) {
            "Archiving is not incremental, but $mode is received"
        }

        val argFile = argsFile.get().asFile
        val argFileArg = args.createArgFile(argFile)
        logArgs("Archiver args", args, argFile)

        execOperations.exec {
            executable = archiver.get()
            workingDir = outDir.get().asFile
            this.args = args.toArray().asList()
        }
    }

    override fun configureArgs() =
        super.configureArgs().apply {
            rawArgs(flags.get())
            arg(null, outDir.resolveToIoFile(libOutputFileName))

            repeatedArg(values = objectFiles.files)
        }
}