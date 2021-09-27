import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations

import javax.inject.Inject

import java.io.File
import java.util.LinkedHashSet

abstract class WasmCrossCompileTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Nested
    abstract val targetOs: Property<OS>

    @get:Nested
    abstract val targetArch: Property<Arch>

    @get:Nested
    abstract val buildVariant: Property<SkiaBuildType>

    @get:Input
    abstract val flags: ListProperty<String>

    @get:InputFiles
    lateinit var cFiles: FileCollection

    @get:InputFiles
    lateinit var aFiles: FileCollection

    @get:Optional
    @get:Input
    abstract val wasmFileName: Property<String>

    @get:Optional
    @get:Input
    abstract val jsFileName: Property<String>

    @get:OutputDirectory
    abstract val outDir: DirectoryProperty

    @get:InputFile
    @get:Optional
    abstract val skikoJsPrefix: RegularFileProperty

    /**
     * Used only for up-to-date checks of headers' content
     *
     * @see [headersDirs]
     */
    @Suppress("UNUSED")
    @get:InputFiles
    val hFiles: FileCollection
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
    fun exec() {
        execOperations.exec {
            executable = "emcc"
            val args = argumentProviders

            args.add { headersDirs.map { "-I${it.absolutePath}" } }

            args.add {
                // todo: ensure that flags do not start with '-I' (all headers should be added via [headersDirs])
                val bt = buildVariant.get()
                (flags.get() + targetArch.get().clangFlags + bt.flags + bt.clangFlags).asIterable()
            }

            args.add { cFiles.files.map { it.absolutePath } }
            args.add { aFiles.files.map { it.absolutePath } }

            args.add { listOf("-o", outDir.resolveToAbsolutePath(wasmFileName)) }
            args.add { listOf("-o", outDir.resolveToAbsolutePath(jsFileName)) }
        }
    }
}