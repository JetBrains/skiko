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
    abstract val skiaDir: DirectoryProperty

    @get:Internal
    abstract val skikoTargetFlags: ListProperty<String>

    @TaskAction
    fun buildSkia() {
        val version = skiaVersion.get()
        val target = skiaTarget.get()
        val type = buildType.get()
        val dir = skiaDir.get().asFile

        // Validate version format
        if (!version.matches(Regex("^m[0-9]+-[0-9a-f]+(-.+)?$"))) {
            logger.warn("SKIA_VERSION '$version' doesn't match expected format m###-sha[-inc]")
        }

        logger.lifecycle("Building Skia $version for target ${target.id} in ${type.id} mode")
        logger.lifecycle("Using Skia directory: ${dir.absolutePath}")

        // Determine host architecture
        val hostArch = when (System.getProperty("os.arch")) {
            "aarch64", "arm64" -> Arch.Arm64
            else -> Arch.X64
        }

        // Run Python scripts for each machine architecture
        val machines = target.machines(hostArch)
        logger.lifecycle("Building for architectures: ${machines.joinToString { it.id }}")

        // Checkout Skia dependencies
        runPythonScript(dir, "checkout.py", "--version", version)

        // Build and archive for each machine
        machines.forEach { machine ->
            logger.lifecycle("Building for ${machine.id}...")
            runPythonScript(
                dir, "build.py",
                "--target", target.id,
                "--machine", machine.id,
                "--build-type", type.id
            )

            logger.lifecycle("Archiving ${machine.id}...")
            runPythonScript(
                dir, "archive.py",
                "--version", version,
                "--target", target.id,
                "--machine", machine.id,
                "--build-type", type.id
            )
        }

        // Clean selective cache
        logger.lifecycle("Cleaning selective cache...")
        project.file("build/classes/kotlin").deleteRecursively()

        // Publish to Maven local with proper flags
        logger.lifecycle("Publishing to Maven Local...")
        val gradleFlags = target.getGradleFlags(hostArch)
        val publishCommand = mutableListOf(
            "./gradlew",
            "publishToMavenLocal"
        ) + gradleFlags + listOf(
            "-Pskia.dir=${dir.absolutePath}",
            "-Pskiko.debug=${type == SkiaBuildType.DEBUG}"
        )

        logger.lifecycle("Running: ${publishCommand.joinToString(" ")}")

        project.exec {
            workingDir = project.projectDir
            commandLine = publishCommand
            isIgnoreExitValue = false
        }

        logger.lifecycle("Successfully published Skia build to Maven Local")
    }

    private fun runPythonScript(dir: File, script: String, vararg args: String) {
        val scriptPath = "tools/skia_release/$script"
        val fullCommand = listOf("python3", scriptPath) + args

        logger.lifecycle("Running: ${fullCommand.joinToString(" ")}")

        val output = ByteArrayOutputStream()
        val result = project.exec {
            workingDir = dir
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
