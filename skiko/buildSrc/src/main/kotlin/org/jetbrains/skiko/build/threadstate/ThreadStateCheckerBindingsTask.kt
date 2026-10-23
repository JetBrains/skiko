package org.jetbrains.skiko.build.threadstate

import java.io.ByteArrayOutputStream
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

abstract class ThreadStateCheckerBindingsTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {
    @get:InputFile
    abstract val executableFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val bindingsFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun extract() {
        val declarations = sortedMapOf<String, Binding>()
        val files = mutableMapOf<String, String>()
        for (file in bindingsFiles.files.sorted()) {
            file.forEachLine { line ->
                val fields = line.split(' ', limit = 3)
                require(fields.size == 3 && fields[1] in setOf("0", "1") && fields[2].isNotEmpty()) {
                    "Invalid binding in $file: $line"
                }
                val name = fields[0]
                val binding = Binding(fields[1] == "1", fields[2])
                val previous = declarations[name]
                require(previous == null || previous.marked == binding.marked) {
                    "Conflicting ForceNativeThreadState annotations for $name in ${previous?.file} (${files[name]}) and " +
                        "${binding.file} ($file); make them agree."
                }
                declarations[name] = binding
                files[name] = file.path
            }
        }
        val stdout = ByteArrayOutputStream()
        execOperations.exec {
            commandLine("nm", "-n", executableFile.get().asFile.absolutePath)
            standardOutput = stdout
        }
        val symbol = Regex("^([0-9a-fA-F]+)\\s+[Tt]\\s+(\\S+)$")
        val bindings = stdout.toString(Charsets.UTF_8.name()).lineSequence().mapNotNull { line ->
            symbol.matchEntire(line.trim())?.let {
                val rawName = it.groupValues[2]
                val name = if (rawName in declarations) rawName else rawName.removePrefix("_")
                declarations[name]?.let { binding ->
                    "${it.groupValues[1]} $name ${if (binding.marked) 1 else 0} ${binding.file}"
                }
            }
        }.distinct().toList()
        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(bindings.joinToString("\n", postfix = if (bindings.isEmpty()) "" else "\n"))
    }
}

internal data class Binding(val marked: Boolean, val file: String)
