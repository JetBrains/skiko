import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject

/**
 * Verifies that emcc (Emscripten compiler) is available on the system.
 * If emcc is not found from previous installations, the task automatically installs the Emscripten SDK
 * into gradles user home directory using the specified version.
 *
 * The installed emsdk location is exposed via [emsdkDir] so that downstream
 * tasks can use the emscripten binaries if needed.
 */
abstract class SetupEmscriptenTask : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    abstract val emsdkVersion: Property<String>

    @get:Input
    abstract val requireExistingEmsdk: Property<Boolean>

    @get:OutputDirectory
    abstract val emsdkDir: DirectoryProperty

    init {
        requireExistingEmsdk.convention(false)
        emsdkDir.convention(
            project.layout.dir(
                emsdkVersion.map { version ->
                    project.rootProject.gradle.gradleUserHomeDir.resolve("emsdk/emsdk-$version")
                }
            )
        )
    }

    @TaskAction
    fun run() {
        // Check if emsdk was previously installed by this task
        if (emccFile().isFile) {
            logger.lifecycle("emcc is already available at: ${emccFile().absolutePath}")
            return
        }

        if (requireExistingEmsdk.get()) {
            throw GradleException("emcc was not found at: ${emccFile().absolutePath}")
        }

        logger.lifecycle("emcc not found. Installing Emscripten SDK ${emsdkVersion.get()}...")
        installEmsdk()
        logger.lifecycle("Emscripten SDK ${emsdkVersion.get()} installed successfully at: ${emsdkDir.get().asFile.absolutePath}")
    }

    private fun emccFile(): File =
        emsdkDir.get().asFile.resolve("upstream/emscripten/${if (hostOs.isWindows) "emcc.bat" else "emcc"}")

    private fun installEmsdk() {
        val version = emsdkVersion.get()
        val sdkDir = emsdkDir.get().asFile

        downloadAndExtractEmsdk(version, sdkDir)

        val emsdkScript = sdkDir.resolve(if (hostOs.isWindows) "emsdk.bat" else "emsdk").absolutePath

        logger.lifecycle("Installing emsdk version $version...")
        exec(emsdkScript, "install", version, workingDir = sdkDir)

        logger.lifecycle("Activating emsdk version $version...")
        exec(emsdkScript, "activate", version, workingDir = sdkDir)
    }

    private fun downloadAndExtractEmsdk(version: String, sdkDir: File) {
        val parentDir = sdkDir.parentFile
        parentDir.mkdirs()

        if (sdkDir.exists() && !sdkDir.deleteRecursively()) {
            throw GradleException("Failed to delete incomplete emsdk directory: ${sdkDir.absolutePath}")
        }

        val archiveFile = Files.createTempFile(parentDir.toPath(), "emsdk-$version-", ".zip").toFile()
        val archiveUrl = "https://github.com/emscripten-core/emsdk/archive/refs/tags/$version.zip"
        try {
            logger.lifecycle("Downloading emsdk source archive from $archiveUrl...")
            URI(archiveUrl).toURL().openStream().use { input ->
                Files.copy(input, archiveFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }

            logger.lifecycle("Extracting emsdk source archive to ${sdkDir.absolutePath}...")
            extractArchive(archiveFile, sdkDir)
        } finally {
            archiveFile.delete()
        }
    }

    private fun extractArchive(archiveFile: File, sdkDir: File) {
        project.copy {
            from(project.zipTree(archiveFile))
            into(sdkDir.parentFile)
        }
    }

    private fun exec(vararg args: String, workingDir: File? = null) {
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
}
