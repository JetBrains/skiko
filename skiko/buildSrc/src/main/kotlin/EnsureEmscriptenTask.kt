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
import javax.inject.Inject

/**
 * Verifies that emcc (Emscripten compiler) is available on the system.
 * If emcc is not found from previous installations, the task automatically installs the Emscripten SDK
 * into gradles user home directory using the specified version and commit hash.
 *
 * The installed emsdk location is exposed via [emsdkDir] so that downstream
 * tasks can use the emscripten binaries if needed.
 */
abstract class EnsureEmscriptenTask : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    abstract val emsdkVersion: Property<String>

    @get:Input
    abstract val emsdkCommit: Property<String>

    @get:Input
    abstract val requireExistingEmsdk: Property<Boolean>

    @get:OutputDirectory
    abstract val emsdkDir: DirectoryProperty

    init {
        requireExistingEmsdk.convention(false)
        emsdkDir.convention(
            project.layout.dir(
                emsdkVersion.zip(emsdkCommit) { version, commit ->
                    project.rootProject.gradle.gradleUserHomeDir.resolve("emsdk/emsdk-$version-$commit")
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
        val commit = emsdkCommit.get()
        val sdkDir = emsdkDir.get().asFile

        // Clone emsdk if needed, then checkout the pinned commit.
        if (sdkDir.resolve(".git").isDirectory) {
            logger.lifecycle("Using existing emsdk clone...")
        } else {
            sdkDir.mkdirs()
            logger.lifecycle("Cloning emsdk repository...")
            exec("git", "clone", "--no-checkout", "https://github.com/emscripten-core/emsdk.git", sdkDir.absolutePath)
        }

        logger.lifecycle("Checking out emsdk commit $commit...")
        exec("git", "fetch", "--depth", "1", "origin", commit, workingDir = sdkDir)
        exec("git", "checkout", "--detach", "FETCH_HEAD", workingDir = sdkDir)

        // Install and activate the specified version
        val emsdkScript = sdkDir.resolve(if (hostOs.isWindows) "emsdk.bat" else "emsdk").absolutePath

        logger.lifecycle("Installing emsdk version $version...")
        exec(emsdkScript, "install", version, workingDir = sdkDir)

        logger.lifecycle("Activating emsdk version $version...")
        exec(emsdkScript, "activate", version, workingDir = sdkDir)
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
