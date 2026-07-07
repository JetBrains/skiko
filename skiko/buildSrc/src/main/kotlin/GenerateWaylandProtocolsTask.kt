import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

/**
 * Generates Wayland protocol bindings from the protocol XML files shipped by `wayland-protocols`.
 *
 * For each input XML this runs `wayland-scanner` twice, producing
 * `<protocol>-client-protocol.h` (client header, consumed by the `waylandegl` cinterop) and
 * `<protocol>-protocol.c` (interface marshalling code, compiled into the native bridges).
 *
 * The marshalling code is emitted with a `.c` extension on purpose: compiling it as a C file
 * ensures the interface globals maintain external linkage (with hidden visibility) so they aren't
 * optimized out by the C++ compiler's internal-linkage-for-const-globals rule.
 */
abstract class GenerateWaylandProtocolsTask @Inject constructor(
    private val execOperations: ExecOperations,
) : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val protocolXmlFiles: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outDir: DirectoryProperty

    @TaskAction
    fun run() {
        val outDirFile = outDir.get().asFile
        outDirFile.mkdirs()
        for (xml in protocolXmlFiles.files) {
            val name = xml.nameWithoutExtension
            execOperations.exec {
                commandLine(
                    "wayland-scanner",
                    "client-header",
                    xml.absolutePath,
                    outDirFile.resolve("$name-client-protocol.h").absolutePath,
                )
            }
            execOperations.exec {
                commandLine(
                    "wayland-scanner",
                    "private-code",
                    xml.absolutePath,
                    outDirFile.resolve("$name-protocol.c").absolutePath,
                )
            }
        }
    }
}
