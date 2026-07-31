import internal.utils.ArgBuilder
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile

abstract class OptimizeSkikoWasmTask : AbstractSkikoNativeToolTask() {

    @get:Input
    abstract val flags: ListProperty<String>

    @get:Input
    abstract val optimizer: Property<String>

    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    override val outDirNameForTool: String
        get() = "optimize"

    override fun execute(mode: ToolMode, args: ArgBuilder) {
        check(mode is ToolMode.NonIncremental) {
            "Optimization is not incremental, but $mode is received"
        }

        logArgs("Optimize args", args)
        outputFile.get().asFile.parentFile.mkdirs()

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
            arg("-o", outputFile)
        }
}