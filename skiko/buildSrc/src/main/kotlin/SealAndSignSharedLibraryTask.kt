import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class SealAndSignSharedLibraryTask : DefaultTask() {
    @get:Inject
    protected abstract val fileOperations: FileOperations

    @get:Optional
    @get:InputFile
    abstract val sealer: RegularFileProperty

    @get:Optional
    @get:InputFile
    abstract val codesignClient: RegularFileProperty

    @get:InputFile
    abstract val libFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outDir: DirectoryProperty

    @get:Internal
    val outputFiles: Provider<List<File>>
        get() = outDir.map { outDir ->
            outDir.asFile.listFiles().orEmpty().filter { it.isFile }
        }

    @get:Optional
    @get:Input
    abstract val signHost: Property<String>

    @get:Optional
    @get:Input
    abstract val signUser: Property<String>

    @get:Optional
    @get:Input
    abstract val signToken: Property<String>

    @TaskAction
    fun run() {
        val outDir = outDir.get().asFile
        fileOperations.delete(outDir)
        fileOperations.mkdir(outDir)

        val libFile = libFile.get().asFile
        val outputFile = outDir.resolve(libFile.name)

        libFile.copyTo(outputFile, overwrite = true)

        sealer.orNull?.let { sealer ->
            sealBinary(sealer.asFile, libFile)
        }

        val signHost = signHost.orNull
        if (signHost == null) {
            logger.info("Skipping signing, because 'signHost' is not set")
        } else {
            remoteSignCodesign(outputFile)
        }
    }

    // See https://github.com/olonho/sealer.
    private fun sealBinary(sealer: File, lib: File) {
        logger.info("Sealing $lib by $sealer")
        val proc = ProcessBuilder(sealer.absolutePath, "-f", lib.absolutePath, "-p", "Java_")
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
        proc.waitFor(2, TimeUnit.MINUTES)
        if (proc.exitValue() != 0) {
            throw GradleException("Cannot seal $lib")
        }
        logger.info("Sealed!")
    }

    private fun remoteSignCodesign(fileToSign: File) {
        val user = signUser.orNull ?: error("signUser is null")
        val token = signToken.orNull ?: error("signToken is null")
        val cmd = arrayOf(
            codesignClient.get().asFile.absolutePath,
            fileToSign.absolutePath
        )
        val procBuilder = ProcessBuilder(*cmd).apply {
            directory(fileToSign.parentFile)
            val env = environment()
            env["SERVICE_ACCOUNT_NAME"] = user
            env["SERVICE_ACCOUNT_TOKEN"] = token
            redirectOutput(ProcessBuilder.Redirect.INHERIT)
            redirectError(ProcessBuilder.Redirect.INHERIT)
        }
        logger.info("Starting remote code sign")
        val proc = procBuilder.start()
        proc.waitFor(5, TimeUnit.MINUTES)
        if (proc.exitValue() != 0) {
            throw GradleException("Failed to sign $fileToSign")
        } else {
            val signedDir = fileToSign.resolveSibling("signed")
            val signedFile = signedDir.resolve(fileToSign.name)
            check(signedFile.exists()) {
                buildString {
                    appendLine("Signed file does not exist: $signedFile")
                    appendLine("Other files in $signedDir:")
                    signedDir.list()?.let { names ->
                        names.forEach {
                            appendLine("  * $it")
                        }
                    }
                }
            }
            val size = signedFile.length()
            if (size < 200 * 1024) {
                val content = signedFile.readText()
                logger.info(content)
                throw GradleException("Output is too short $size: ${content.take(200)}...")
            } else {
                signedFile.copyTo(fileToSign, overwrite = true)
                signedDir.deleteRecursively()
                logger.info("Successfully signed $fileToSign")
            }
        }
    }
}
