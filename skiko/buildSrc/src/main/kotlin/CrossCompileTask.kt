import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

abstract class CrossCompileTask : org.gradle.api.tasks.Exec() {
    @get:Input
    abstract val compiler: Property<String>

    @get:Input
    abstract val targetOs: Property<OS>

    @get:Input
    abstract val targetArch: Property<Arch>

    @get:Input
    abstract val buildVariant: Property<SkiaBuildType>

    @get:Input
    abstract val flags: ListProperty<String>

    @get:Input
    abstract val outputFile: Property<String>

    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:InputDirectory
    abstract val skiaDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        compiler.convention("clang++")
        buildVariant.convention(SkiaBuildType.RELEASE)
    }

    override fun exec() {
        executable = compiler.get()
        argumentProviders.add {
            val bt = buildVariant.get()
            (flags.get() + /* target_arch.get().clangFlags + */
                    bt.flags + bt.clangFlags).asIterable()
        }
        argumentProviders.add {
            inputDir.get().asFileTree.files.filter { it.name.endsWith(".cc") }.map { it.absolutePath }
        }
        argumentProviders.add {
            listOf("-o", outputFile.get())
        }
        argumentProviders.add {
            skiaDir.get().asFileTree.files.filter { it.name.endsWith(".a") }.map { it.absolutePath }
        }
        workingDir = targetOutputDir
        workingDir.mkdirs()
        super.exec()
    }

    // TODO: is it correct?
    @get:OutputDirectory
    val targetOutputDir: File
        get() = outputDir.get().asFile.absoluteFile.resolve("cc/${targetOs.get().id}_${targetArch.get().id}")
}