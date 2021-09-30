import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.work.ChangeType
import org.gradle.work.FileChange
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.io.File
import java.util.LinkedHashSet
import java.util.concurrent.Callable
import javax.inject.Inject

abstract class CrossCompileTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    @get:Optional
    abstract val crossCompileTargetOS: Property<OS>

    @get:Input
    abstract val crossCompileTargetArch: Property<Arch>

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
    @get:Incremental
    val headerFiles: FileCollection =
        project.files(Callable {
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
            headers
        })

    @get:Internal
    internal val headersDirs = LinkedHashSet<File>()
    fun includeHeadersNonRecursive(dirs: Collection<File>) {
        headersDirs.addAll(dirs)
    }
    fun includeHeadersNonRecursive(dir: File) {
        headersDirs.add(dir)
    }

    @TaskAction
    open fun run(inputChanges: InputChanges) {
        execOperations.exec {
            val sourcesToCompile = determineSourcesToCompile(inputChanges)
            executable = compiler.get()
            args = arrayListOf<String>().also { args ->
                configureCompilerArgs(args)
                args.addAll(sourcesToCompile.map { it.absolutePath })
            }

            workingDir = outDir.get().asFile
            // todo: log args to file system
        }
    }

    private sealed class Mode {
        class Incremental(val changes: Iterable<FileChange>) : Mode()
        class NonIncremental(val reason: String) : Mode()
    }

    private fun determineSourcesToCompile(inputChanges: InputChanges): Collection<File> {
        val compilationMode = analyzeIncrementalChanges(inputChanges)
        val outDir = outDir.get().asFile
        return when (compilationMode) {
            is Mode.Incremental -> {
                val sourcesToCompile = arrayListOf<File>()
                for (change in compilationMode.changes) {
                    val outdatedOutputFile = outDir.resolve(change.file.nameWithoutExtension + ".o")
                    if (outdatedOutputFile.exists()) {
                        // `outdatedOutputFile` might not exist,
                        // when `change.file` is a new file, which was not compiled
                        // todo: log in verbose mode
                        outdatedOutputFile.delete()
                    }
                    if (change.changeType != ChangeType.REMOVED) {
                        sourcesToCompile.add(change.file)
                    }
                }
                logger.warn("Compiling ${sourcesToCompile.size} files incrementally")
                sourcesToCompile
            }
            is Mode.NonIncremental -> {
                outDir.deleteRecursively()
                outDir.mkdirs()
                logger.warn("Recompiling all files: ${compilationMode.reason}")
                sourceFiles.files
            }
        }
    }
    private fun analyzeIncrementalChanges(inputChanges: InputChanges): Mode {
        if (!inputChanges.isIncremental) {
            return Mode.NonIncremental("input changes are not incremental")
        }

        if (inputChanges.getFileChanges(headerFiles).any()) {
            return Mode.NonIncremental("header files are modified or removed")
        }

        return Mode.Incremental(inputChanges.getFileChanges(sourceFiles))
    }

    open fun configureCompilerArgs(args: MutableList<String>) {
        args.add("-c")
        args.addAll(headersDirs.map { "-I${it.absolutePath}" })

        // todo: ensure that flags do not start with '-I' (all headers should be added via [headersDirs])
        args.addAll(flags.get())
        args.addAll(crossCompileTargetOS.orNull?.clangFlags ?: arrayOf())
        args.addAll(crossCompileTargetArch.get().clangFlags)
        val bt = buildVariant.get()
        args.addAll(bt.flags)
        args.addAll(bt.clangFlags)
    }
}