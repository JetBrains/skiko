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
    abstract val skiaRepoDir: DirectoryProperty

    @get:Internal
    abstract val skikoTargetFlags: ListProperty<String>

    @TaskAction
    fun buildSkia() {
        val version = skiaVersion.get()
        val target = skiaTarget.get()
        val type = buildType.get()
        val skiaRepoRoot = skiaRepoDir.get().asFile

        val scriptsDir = File(skiaRepoRoot, "tools/skia_release")
        if (!scriptsDir.isDirectory) {
            throw GradleException(
                "Directory tools/skia_release not found in ${skiaRepoRoot.absolutePath}\n" +
                "Expected: skia repository root containing tools/skia_release/\n" +
                "Ensure skia.repo.dir points to the correct skia checkout"
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
        logger.lifecycle("Using skia repository: ${skiaRepoRoot.absolutePath}")
        logger.lifecycle("Using scripts directory: tools/skia_release")

        // Determine host architecture
        val hostArch = when (System.getProperty("os.arch")) {
            "aarch64", "arm64" -> Arch.Arm64
            else -> Arch.X64
        }

        // Run Python scripts for each machine architecture
        val machines = target.machines(hostArch)
        logger.lifecycle("Building for architectures: ${machines.joinToString { it.id }}")

        // Build and archive for each machine
        machines.forEach { machine ->
            logger.lifecycle("Building for ${machine.id}...")
            runPythonScript(
                skiaRepoRoot, "build.py",
                "--skia-dir", ".",
                "--target", target.id,
                "--machine", machine.id,
                "--build-type", type.id
            )

            logger.lifecycle("Archiving ${machine.id}...")
            runPythonScript(
                skiaRepoRoot, "archive.py",
                "--skia-dir", ".",
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
        logger.lifecycle("Next: Run './gradlew publishToMavenLocal -Pskia.dir=<skia-repo-dir>' to publish")
    }

    private fun runPythonScript(skiaRepoRoot: File, script: String, vararg args: String) {
        val scriptPath = "tools/skia_release/$script"
        val scriptFile = File(skiaRepoRoot, scriptPath)

        // Validate script file exists
        if (!scriptFile.exists()) {
            throw GradleException(
                "Python script not found: ${scriptFile.absolutePath}\n" +
                "Expected: skia directory with tools/skia_release/$script\n" +
                "Ensure skia.repo.dir points to the correct skia checkout"
            )
        }

        val fullCommand = listOf("python3", scriptPath) + args

        logger.lifecycle("Running: ${fullCommand.joinToString(" ")}")

        val output = ByteArrayOutputStream()
        val result = project.exec {
            workingDir = skiaRepoRoot
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
