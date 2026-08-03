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
 * Verifies that emcc (Emscripten compiler) is available on the system PATH.
 * If emcc is not found, the task automatically installs the Emscripten SDK
 * into the project build directory using the specified version.
 *
 * The installed emsdk location is exposed via [emsdkDir] so that downstream
 * tasks can add the emscripten binaries to their PATH if needed.
 */
abstract class EnsureEmscriptenTask : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Input
    abstract val emsdkVersion: Property<String>

    @get:OutputDirectory
    abstract val emsdkDir: DirectoryProperty

    init {
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
        if (isEmccAvailable()) {
            logger.lifecycle("emcc is already available available.")
            return
        }

        logger.lifecycle("emcc not found. Installing Emscripten SDK ${emsdkVersion.get()}...")
        installEmsdk()
        logger.lifecycle("Emscripten SDK ${emsdkVersion.get()} installed successfully at: ${emsdkDir.get().asFile.absolutePath}")
    }

    private fun isEmccAvailable(): Boolean {
        val emccName = if (isWindows()) "emcc.bat" else "emcc"

        // Check if emsdk was previously installed by this task
        val localEmcc = emsdkDir.get().asFile.resolve("upstream/emscripten/$emccName")
        return localEmcc.isFile
    }

    private fun installEmsdk() {
        val version = emsdkVersion.get()
        val sdkDir = emsdkDir.get().asFile

        // Clone or update emsdk
        if (sdkDir.resolve(".git").isDirectory) {
            logger.lifecycle("Updating existing emsdk clone...")
            exec("git", "pull", workingDir = sdkDir)
        } else {
            sdkDir.mkdirs()
            logger.lifecycle("Cloning emsdk repository...")
            exec("git", "clone", "https://github.com/emscripten-core/emsdk.git", sdkDir.absolutePath)
        }

        // Install and activate the specified version
        val emsdkScript = if (isWindows()) sdkDir.resolve("emsdk.bat").absolutePath
                          else sdkDir.resolve("emsdk").absolutePath

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

    private fun isWindows(): Boolean =
        System.getProperty("os.name").startsWith("Win", ignoreCase = true)
}
