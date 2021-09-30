import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.work.Incremental
import java.io.File
import java.util.LinkedHashSet
import javax.inject.Inject

abstract class CrossCompileTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    @get:Optional
    abstract val targetOs: Property<OS>

    @get:Input
    abstract val targetArch: Property<Arch>

    @get:Input
    abstract val buildVariant: Property<SkiaBuildType>

    @get:Input
    abstract val flags: ListProperty<String>

    @get:InputFiles
    @get:Incremental
    lateinit var sourceFiles: FileCollection

    @get:OutputDirectory
    abstract val outDir: DirectoryProperty

    @get:Input
    abstract val compiler: Property<String>

    /**
     * Used only for up-to-date checks of headers' content
     *
     * @see [headersDirs]
     */
    @Suppress("UNUSED")
    @get:InputFiles
    val headerFiles: FileCollection
        get() {
            fun File.isHeaderFile(): Boolean =
                isFile && name.endsWith(".h", ignoreCase = true)

            val headers = hashSetOf<File>()
            for (dir in headersDirs) {
                val canonicalDir = dir.canonicalFile
                dir.listFiles()?.forEach { file ->
                    if (file.isHeaderFile()) {
                        headers.add(canonicalDir.resolve(file.name))
                    }
                }
            }
            return project.files(headers)
        }

    @get:Internal
    internal val headersDirs = LinkedHashSet<File>()
    fun includeHeadersNonRecursive(dirs: Collection<File>) {
        headersDirs.addAll(dirs)
    }
    fun includeHeadersNonRecursive(dir: File) {
        headersDirs.add(dir)
    }

    @TaskAction
    open fun run() {
        execOperations.exec {
            executable = compiler.get()
            args = arrayListOf<String>().also { args ->
                configureCompilerArgs(args)
            }

            workingDir = outDir.get().asFile
            // todo: log args to file system
        }
    }

    open fun configureCompilerArgs(args: MutableList<String>) {
        args.add("-c")
        args.addAll(headersDirs.map { "-I${it.absolutePath}" })

        // todo: ensure that flags do not start with '-I' (all headers should be added via [headersDirs])
        args.addAll(flags.get())
        args.addAll(targetOs.orNull?.clangFlags ?: arrayOf())
        args.addAll(targetArch.get().clangFlags)
        val bt = buildVariant.get()
        args.addAll(bt.flags)
        args.addAll(bt.clangFlags)

        args.addAll(sourceFiles.map { it.absolutePath })
    }
}