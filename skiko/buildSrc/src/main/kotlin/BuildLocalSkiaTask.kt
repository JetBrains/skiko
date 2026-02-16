import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.ByteArrayOutputStream
import java.io.File

abstract class BuildLocalSkiaTask : DefaultTask() {

    @get:Input
    abstract val skiaVersion: Property<String>

    @get:Input
    abstract val skiaTarget: Property<SkiaTarget>

    @get:Input
    abstract val buildType: Property<SkiaBuildType>

    @get:InputDirectory
    abstract val skiaPackDir: DirectoryProperty

    @get:Internal
    abstract val skikoTargetFlags: ListProperty<String>

    @TaskAction
    fun buildSkia() {
        val version = skiaVersion.get()
        val target = skiaTarget.get()
        val type = buildType.get()
        val skiaPackRoot = skiaPackDir.get().asFile

        // Validate that skiaPackDir points to skia-pack repository root
        val scriptsDir = File(skiaPackRoot, "script")
        if (!scriptsDir.isDirectory) {
            throw GradleException(
                "Directory script not found in ${skiaPackRoot.absolutePath}\n" +
                "Expected: skia-pack repository root containing script/\n" +
                "Ensure skia.pack.dir points to the correct skia-pack directory"
            )
        }

        // Validate version format
        if (!version.matches(Regex("^m[0-9]+-[0-9a-f]+(-.+)?$"))) {
            throw GradleException(
                "Invalid SKIA_VERSION format: '$version'\n" +
                "Expected format: m###-commit_sha[-increment]\n" +
                "Example: m138-80d088a-2"
            )
        }

        logger.lifecycle("Building Skia $version for target ${target.id} in ${type.id} mode")
        logger.lifecycle("Using skia-pack directory: ${skiaPackRoot.absolutePath}")
        logger.lifecycle("Using scripts directory: script")

        // Determine host architecture
        val hostArch = when (System.getProperty("os.arch")) {
            "aarch64", "arm64" -> Arch.Arm64
            else -> Arch.X64
        }

        // Run Python scripts for each machine architecture
        val machines = target.machines(hostArch)
        logger.lifecycle("Building for architectures: ${machines.joinToString { it.id }}")

        // Checkout Skia dependencies
        runPythonScript(skiaPackRoot, "checkout.py", "--version", version)

        // Build and archive for each machine
        machines.forEach { machine ->
            logger.lifecycle("Building for ${machine.id}...")
            runPythonScript(
                skiaPackRoot, "build.py",
                "--target", target.id,
                "--machine", machine.id,
                "--build-type", type.id
            )

            logger.lifecycle("Archiving ${machine.id}...")
            runPythonScript(
                skiaPackRoot, "archive.py",
                "--version", version,
                "--target", target.id,
                "--machine", machine.id,
                "--build-type", type.id
            )
        }

        // Clean selective cache
        logger.lifecycle("Cleaning selective cache...")
        project.file("build/classes/kotlin").deleteRecursively()

        logger.lifecycle("Skia binaries built successfully")
        logger.lifecycle("Next: Run './gradlew publishToMavenLocal -Pskia.dir=<skia-source-dir>' to publish")
    }

    private fun runPythonScript(skiaPackRoot: File, script: String, vararg args: String) {
        val scriptPath = "script/$script"
        val scriptFile = File(skiaPackRoot, scriptPath)

        // Validate script file exists
        if (!scriptFile.exists()) {
            throw GradleException(
                "Python script not found: ${scriptFile.absolutePath}\n" +
                "Expected: skia-pack directory with script/$script\n" +
                "Ensure skia.pack.dir points to correct skia-pack directory"
            )
        }

        val fullCommand = listOf("python3", scriptPath) + args

        logger.lifecycle("Running: ${fullCommand.joinToString(" ")}")

        val output = ByteArrayOutputStream()
        val result = project.exec {
            workingDir = skiaPackRoot
            commandLine = fullCommand
            standardOutput = output
            errorOutput = output
            isIgnoreExitValue = true
        }

        val outputStr = output.toString()
        if (outputStr.isNotEmpty()) {
            logger.lifecycle(outputStr)
        }

        if (result.exitValue != 0) {
            throw GradleException(
                "Python script $script failed with exit code ${result.exitValue}"
            )
        }
    }
}
