import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject

/** Merges Skia's static archives, retaining one copy of each object. */
abstract class MergeStaticArchivesTask : DefaultTask() {
    @get:Inject abstract val execOperations: ExecOperations
    @get:Input abstract val targetOs: Property<OS>
    @get:InputFiles abstract val archives: ListProperty<File>
    @get:OutputFile abstract val output: RegularFileProperty

    @TaskAction fun merge() {
        val inputs = archives.get().filter(File::isFile)
        require(inputs.isNotEmpty()) { "No static archives to merge" }
        val dir = temporaryDir.apply { deleteRecursively(); mkdirs() }
        val outputFile = output.get().asFile
        val os = targetOs.get()
        val useXcrun = when (os) {
            OS.Linux -> false
            OS.MacOS, OS.IOS, OS.TVOS -> true
            else -> error("Unsupported OS for static archive merging: $os")
        }
        // Apple archives may be fat: one file contains a separate archive (a "slice")
        // for each CPU architecture. `ar` can only unpack a single-architecture archive,
        // so deduplicate each slice independently before combining them with `lipo -create`.
        val archs = if (useXcrun) {
            capture("xcrun", "lipo", "-archs", inputs.first().absolutePath).trim().split(' ')
        } else {
            listOf(os.id)
        }
        val arCommand = if (useXcrun) arrayOf("xcrun", "ar") else arrayOf("ar")
        val slices = archs.map { arch ->
            val members = collectMembers(inputs, arch, archs.size > 1, dir, arCommand)
            val merged = File(dir, "merged-$arch.a")
            run(*arCommand, "-crs", merged.absolutePath, *members.map(File::getAbsolutePath).toTypedArray())
            merged
        }
        outputFile.parentFile.mkdirs()
        if (useXcrun) {
            run("xcrun", "lipo", "-create", *slices.map(File::getAbsolutePath).toTypedArray(), "-output", outputFile.absolutePath)
        } else {
            // `ar` updates an existing archive without removing obsolete members, so build the
            // archive in the cleaned temporary directory and replace the previous output.
            Files.move(slices.single().toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun collectMembers(
        inputs: List<File>,
        arch: String,
        isFat: Boolean,
        dir: File,
        arCommand: Array<String>
    ): List<File> {
        val sha256 = MessageDigest.getInstance("SHA-256")
        val seen = hashSetOf<String>()
        val members = mutableListOf<File>()
        inputs.forEachIndexed { index, input ->
            val slice = if (isFat) {
                File(dir, "$index-$arch.a").also {
                    run("xcrun", "lipo", input.absolutePath, "-thin", arch, "-output", it.absolutePath)
                }
            } else {
                input
            }
            val objects = File(dir, "$index-$arch").apply { mkdirs() }
            run(*arCommand, "-x", slice.absolutePath, cwd = objects)
            objects.listFiles()
                .orEmpty()
                .filter { it.isFile && !it.name.startsWith("__.SYMDEF") }
                .forEach { objectFile ->
                    val digest = Base64.getEncoder().encodeToString(sha256.digest(objectFile.readBytes()))
                    if (seen.add(digest)) members += objectFile
                }
        }
        return members
    }

    private fun run(vararg args: String, cwd: File? = null) = execOperations.exec {
        commandLine(*args)
        workingDir = cwd
    }
    private fun capture(vararg args: String): String {
        val out = ByteArrayOutputStream()
        execOperations.exec { commandLine(*args); standardOutput = out }
        return out.toString()
    }
}
