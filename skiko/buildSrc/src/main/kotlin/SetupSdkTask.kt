import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject

enum class SdkArchiveType {
    ZIP,
    TAR_GZ,
}

abstract class SetupSdkTask : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    abstract val sdkVersion: Property<String>

    @get:Input
    abstract val requireExistingSdk: Property<Boolean>

    @get:OutputDirectory
    abstract val sdkDir: DirectoryProperty

    @get:Internal
    protected abstract val sdkName: String

    init {
        requireExistingSdk.convention(false)
    }

    @TaskAction
    fun run() {
        if (installedMarker().isFile && installedMarker().length() > 0) {
            logger.lifecycle("$sdkName is already available at: ${installedMarker().absolutePath}")
            return
        }

        if (requireExistingSdk.get()) {
            throw GradleException("$sdkName was not found at: ${installedMarker().absolutePath}")
        }

        logger.lifecycle("$sdkName not found. Installing $sdkName ${sdkVersion.get()}...")
        installSdk()
        logger.lifecycle("$sdkName ${sdkVersion.get()} installed successfully at: ${sdkDir.get().asFile.absolutePath}")
    }

    protected abstract fun installedMarker(): File

    protected abstract fun installSdk()

    protected fun downloadAndExtractSdk(archiveUrl: String, archiveType: SdkArchiveType, sdkDir: File) {
        val parentDir = sdkDir.parentFile
        parentDir.mkdirs()

        if (sdkDir.exists() && !sdkDir.deleteRecursively()) {
            throw GradleException("Failed to delete incomplete $sdkName directory: ${sdkDir.absolutePath}")
        }

        val archiveFile = Files.createTempFile(parentDir.toPath(), "${sdkDir.name}-", archiveSuffix(archiveType)).toFile()
        try {
            logger.lifecycle("Downloading $sdkName archive from $archiveUrl...")
            URI(archiveUrl).toURL().openStream().use { input ->
                Files.copy(input, archiveFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }

            logger.lifecycle("Extracting $sdkName archive to ${sdkDir.absolutePath}...")
            when (archiveType) {
                SdkArchiveType.ZIP -> project.copy {
                    from(project.zipTree(archiveFile))
                    into(parentDir)
                }
                SdkArchiveType.TAR_GZ -> exec("tar", "-xzf", archiveFile.absolutePath, "-C", parentDir.absolutePath)
            }
        } finally {
            archiveFile.delete()
        }
    }

    protected fun exec(vararg args: String, workingDir: File? = null) {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execOperations.exec {
            commandLine(*args)
            if (workingDir != null) {
                this.workingDir = workingDir
            }
            standardOutput = stdout
            errorOutput = stderr
            isIgnoreExitValue = true
        }
        val stdoutStr = stdout.toString().trim()
        val stderrStr = stderr.toString().trim()
        if (stdoutStr.isNotEmpty()) logger.lifecycle(stdoutStr)
        if (stderrStr.isNotEmpty()) logger.warn(stderrStr)

        if (result.exitValue != 0) {
            throw GradleException(
                "Command '${args.joinToString(" ")}' failed with exit code ${result.exitValue}.\n" +
                    "stdout: $stdoutStr\n" +
                    "stderr: $stderrStr"
            )
        }
    }

    private fun archiveSuffix(archiveType: SdkArchiveType): String =
        when (archiveType) {
            SdkArchiveType.ZIP -> ".zip"
            SdkArchiveType.TAR_GZ -> ".tar.gz"
        }

}
