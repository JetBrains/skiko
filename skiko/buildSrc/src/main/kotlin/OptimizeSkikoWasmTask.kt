import internal.utils.ArgBuilder
import internal.utils.resolveToIoFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile

abstract class OptimizeSkikoWasmTask : AbstractSkikoNativeToolTask() {

    @get:Input
    abstract val flags: ListProperty<String>

    @get:Input
    abstract val optimizer: Property<String>

    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @get:Input
    abstract val libOutputFileName: Property<String>

    override val outDirNameForTool: String
        get() = "optimize"

    override fun execute(mode: ToolMode, args: ArgBuilder) {
        check(mode is ToolMode.NonIncremental) {
            "Optimization is not incremental, but $mode is received"
        }

        logArgs("Optimize args", args)

        execOperations.exec {
            executable = optimizer.get()
            workingDir = outDir.get().asFile
            this.args = args.toArray().toList()
        }
    }

    override fun configureArgs() =
        super.configureArgs().apply {
            arg(value = inputFile)
            rawArgs(flags.get())
            arg("-o", outDir.resolveToIoFile(libOutputFileName))
        }
}