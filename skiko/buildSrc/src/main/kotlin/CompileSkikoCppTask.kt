import internal.utils.*

import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.gradle.workers.WorkerExecutionException

import java.io.File
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.HashSet

abstract class CompileSkikoCppTask() : AbstractSkikoNativeToolTask() {
    @get:Internal
    open val srcExtensions: Array<String> = arrayOf("cc")

    @get:Internal
    open val headerExtensions: Array<String> = arrayOf("h", "hh")

    @get:Input
    abstract val flags: ListProperty<String>

    @get:Internal
    abstract val sourceRoots: ListProperty<Directory>

    @get:Input
    abstract val compiler: Property<String>

    override val outDirNameForTool: String
        get() = "compile"

    @get:InputFiles
    @get:Incremental
    val sourceFiles: FileCollection =
        project.files(Callable {
            val sources = project.objects.fileCollection()
            for (sourceRoot in sourceRoots.get()) {
                for (srcExtension in srcExtensions) {
                    sources.from(project.fileTree(sourceRoot) { include("**/*.$srcExtension") })
                }
            }
            sources
        })

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
            val headers = project.objects.fileCollection()
            for (dir in headersDirs) {
                headers.from(project.fileTree(dir) {
                    // headers from include dirs should be included non-recursively
                    for (headerExtension in headerExtensions) {
                        include("*.$headerExtension")
                    }
                })
            }
            for (sourceRoot in sourceRoots.get()) {
                headers.from(project.fileTree(sourceRoot) {
                    // headers from source roots should be included recursively
                    for (headerExtension in headerExtensions) {
                        include("*.$headerExtension")
                    }
                })
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

    private val sourceToOutputMapping = SourceToOutputMapping()
    private val sourceToOutputMappingFile: File
        get() = taskStateDir.get().asFile.resolve("source-to-output.txt")

    override fun beforeRun() {
        if (sourceToOutputMappingFile.exists()) {
            sourceToOutputMapping.load(sourceToOutputMappingFile)
        }
    }

    override fun afterRun() {
        sourceToOutputMapping.save(sourceToOutputMappingFile)
    }

    private val compilerArgsRootDir = taskStateDir.map { it.dir("args") }

    override fun createArgBuilder(): ArgBuilder =
        if (buildTargetOS.get().isWindows) VisualCppCompilerArgBuilder()
        else super.createArgBuilder()

    override fun execute(mode: ToolMode, args: ArgBuilder) {
        val sourcesToCompile: Collection<File> = when (mode) {
            is ToolMode.Incremental -> mode.newOrModifiedFiles()
            is ToolMode.NonIncremental -> sourceFiles.files.also {
                logger.warn("Performing non-incremental compilation: ${mode.reason}")
            }
        }
        updateSourcesToOutputsMapping(sourcesToCompile)

        val outDir = outDir.get().asFile
        val compilerExecutablePath = findCompilerExecutable().absolutePath
        val workQueue = workerExecutor.noIsolation()
        val submittedWorks = HashSet<String>()

        val sourceOutputPairs = sourcesToCompile.map { sourceFile ->
            // check all output files and their parent dirs before compiling anything
            val outputFile = sourceToOutputMapping[sourceFile]
                ?: error("Could not find output file for source file: $sourceFile")
            outputFile.parentFile.mkdirs()
            check(!outputFile.exists()) {
                "Output file should not exist before compilation: '$outputFile'"
            }
            sourceFile to outputFile
        }

        val argFilesDir = compilerArgsRootDir.get().asFile
        cleanDirs(argFilesDir)
        val commonArgsFile = argFilesDir.parentFile.resolve("common-args.txt")
        args.createArgFile(commonArgsFile)
        logArgs("Compiler args", args, commonArgsFile)

        for ((sourceFile, outputFile) in sourceOutputPairs) {
            val workId = "Compiling '${sourceFile.absolutePath}'"
            submittedWorks.add(workId)

            val workArgs = args.copy {
                arg("-o", outputFile)
                arg(value = sourceFile)
            }

            val argFile = run {
                val relative = outputFile.parentFile.relativeTo(outDir).path
                val argFileDir = argFilesDir.resolve(relative)
                argFileDir.resolve("${outputFile.nameWithoutExtension}-args.txt")
            }
            workQueue.submit(RunExternalProcessWork::class.java) {
                this.workId = workId
                executable = compilerExecutablePath
                workingDir = outDir
                this.args = listOf(workArgs.createArgFile(argFile))
            }
        }

        try {
            workQueue.await()
        } catch (e: WorkerExecutionException) {
            for (work in submittedWorks) {
                val result = RunExternalProcessWork.workResults[work]
                if (result == null) {
                    logger.error("Error: no results for work '$work'")
                } else if (result.failure != null) {
                    logger.warn("\n$work:")
                    result.log.flushTo(logger)
                }
            }
            error("Some files were not compiled. Check the log for more details")
        } finally {
            for (work in submittedWorks) {
                RunExternalProcessWork.workResults.remove(work)
            }
        }
    }

    private fun findCompilerExecutable(): File {
        val compilerNameOrFile = compiler.get()
        val compilerFile = File(compilerNameOrFile)
        if (compilerFile.isFile) return compilerFile

        val paths = System.getenv("PATH").split(File.pathSeparator)
        for (path in paths) {
            val file = File(path).resolve(compilerNameOrFile)
            if (file.isFile) return file
        }

        error("Could not find compiler '$compilerNameOrFile' in PATH: $paths")
    }

    override fun cleanStaleOutput(mode: ToolMode.NonIncremental) {
        super.cleanStaleOutput(mode)
        // file is deleted by the base class
        check(!sourceToOutputMappingFile.exists())
        sourceToOutputMapping.clear()
    }

    override fun cleanStaleOutput(mode: ToolMode.Incremental) {
        for (sourceFile in mode.outdatedFiles()) {
            val outdatedOutputFile = sourceToOutputMapping.remove(sourceFile)
            check(outdatedOutputFile != null) {
                "Could not find output file for source file: $sourceFile"
            }
            check(outdatedOutputFile.exists()) {
                "Expected outdated output file does not exist: $outdatedOutputFile"
            }
            outdatedOutputFile.delete()
        }
    }

    private fun updateSourcesToOutputsMapping(sourcesToCompile: Collection<File>) {
        val mappingsForNewOrModifiedFiles = mapSourceFilesToOutputFiles(
            sourceRoots = sourceRoots.get().map { it.asFile },
            sourceFiles = sourcesToCompile,
            outDir = outDir.get().asFile,
            sourceFileExt = ".cc",
            outputFileExt = ".o"
        )
        sourceToOutputMapping.putAll(mappingsForNewOrModifiedFiles)
    }

    override fun determineToolMode(inputChanges: InputChanges): ToolMode {
        if (!sourceToOutputMappingFile.exists()) {
            return ToolMode.NonIncremental("first build or clean build")
        }

        if (!inputChanges.isIncremental) {
            return ToolMode.NonIncremental("inputs' changes are not incremental")
        }

        if (inputChanges.getFileChanges(headerFiles).any()) {
            return ToolMode.NonIncremental("header files are modified or removed")
        }

        val removedFiles = arrayListOf<File>()
        val newFiles = arrayListOf<File>()
        val modifiedFiles = arrayListOf<File>()

        val sourceFilesChanges = inputChanges.getFileChanges(sourceFiles)
        for (change in sourceFilesChanges) {
            when (change.changeType) {
                ChangeType.ADDED -> newFiles.add(change.file)
                ChangeType.MODIFIED -> modifiedFiles.add(change.file)
                ChangeType.REMOVED -> removedFiles.add(change.file)
            }
        }

        return ToolMode.Incremental(
            removedFiles = removedFiles,
            newFiles = newFiles,
            modifiedFiles = modifiedFiles
        )
    }

    override fun configureArgs() =
        super.configureArgs().apply {
            arg("-c")
            repeatedArg("-I", headersDirs)
            // todo: ensure that flags do not start with '-I' (all headers should be added via [headersDirs])
            rawArgs(flags.get())
        }
}

abstract class CompileSkikoObjCTask : CompileSkikoCppTask() {
    override val srcExtensions = arrayOf("mm")

    override val outDirNameForTool: String
        get() = "compileObjC"
}
