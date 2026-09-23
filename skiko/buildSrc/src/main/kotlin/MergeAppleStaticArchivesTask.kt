import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject

/** Merges Skia's Apple static archives, retaining one copy of each object. */
abstract class MergeAppleStaticArchivesTask : DefaultTask() {
    @get:Inject abstract val execOperations: ExecOperations
    @get:InputFiles abstract val archives: ListProperty<File>
    @get:OutputFile abstract val output: RegularFileProperty

    @TaskAction fun merge() {
        val inputs = archives.get().filter(File::isFile)
        require(inputs.isNotEmpty()) { "No static archives to merge" }
        val dir = temporaryDir.apply { deleteRecursively(); mkdirs() }
        val outputFile = output.get().asFile
        // Apple archives may be fat: one file contains a separate archive (a "slice")
        // for each CPU architecture. `ar` can only unpack a single-architecture archive,
        // so deduplicate each slice independently before combining them with `lipo -create`.
        val archs = capture("xcrun", "lipo", "-archs", inputs.first().absolutePath).trim().split(' ')
        val slices = archs.map { arch ->
            val members = collectMembers(inputs, arch, archs.size > 1, dir)
            val merged = File(dir, "merged-$arch.a")
            run("xcrun", "ar", "-crs", merged.absolutePath, *members.map(File::getAbsolutePath).toTypedArray())
            merged
        }
        outputFile.parentFile.mkdirs()
        run("xcrun", "lipo", "-create", *slices.map(File::getAbsolutePath).toTypedArray(), "-output", outputFile.absolutePath)
    }

    private fun collectMembers(inputs: List<File>, arch: String, isFat: Boolean, dir: File): List<File> {
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
            run("xcrun", "ar", "-x", slice.absolutePath, cwd = objects)
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
