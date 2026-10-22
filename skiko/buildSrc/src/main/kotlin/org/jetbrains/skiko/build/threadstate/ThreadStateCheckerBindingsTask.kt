package org.jetbrains.skiko.build.threadstate

import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

abstract class ThreadStateCheckerBindingsTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {
    @get:InputFile
    abstract val executableFile: RegularFileProperty

    @get:InputFiles
    abstract val sourceFiles: ConfigurableFileCollection

    @get:Internal
    abstract val moduleDirectory: DirectoryProperty

    @get:Internal
    abstract val rootModuleDirectory: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun extract() {
        val declarations = scanBindings(
            sourceFiles.files, listOf(moduleDirectory.get().asFile, rootModuleDirectory.get().asFile)
        )
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

internal fun scanBindings(files: Set<File>, bases: List<File>): Map<String, Binding> {
    val table = sortedMapOf<String, Binding>()
    val commentsAndStrings = Regex("\"\"\"[\\s\\S]*?\"\"\"|\"(?:\\\\.|[^\"\\\\])*\"|//[^\\n]*|/\\*[\\s\\S]*?\\*/")
    val strings = Regex("\"\"\"[\\s\\S]*?\"\"\"|\"(?:\\\\.|[^\"\\\\])*\"")
    val symbol = Regex("@(?:[A-Za-z_][\\w]*\\.)*ExternalSymbolName\\s*\\(\\s*\"([^\"]+)\"\\s*\\)")
    val mark = Regex("@(?:[A-Za-z_][\\w]*\\.)*ForceNativeThreadState\\b")
    val function = Regex("\\bfun\\s")
    for (file in files.sorted()) {
        val source = commentsAndStrings.replace(file.readText()) { match ->
            if (match.value.startsWith("\"")) match.value else match.value.map { if (it == '\n') '\n' else ' ' }.joinToString("")
        }
        val annotations = StringBuilder()
        var depth = 0
        for (line in source.lineSequence()) {
            if (line.isBlank()) continue
            val code = strings.replace(line) { " ".repeat(it.value.length) }
            val declaration = function.find(code)
            if (depth > 0) {
                annotations.append(line).append('\n')
                depth += code.count { it == '(' } - code.count { it == ')' }
            } else if (declaration != null) {
                val block = annotations.toString() + line.take(declaration.range.first)
                for (match in symbol.findAll(block)) {
                    val name = match.groupValues[1]
                    val annotationCode = strings.replace(block) { " ".repeat(it.value.length) }
                    val base = bases.filter { file.toPath().startsWith(it.toPath()) }.maxBy { it.path.length }
                    val binding = Binding(
                        mark.containsMatchIn(annotationCode), file.relativeTo(base).invariantSeparatorsPath
                    )
                    val previous = table[name]
                    require(previous == null || previous.marked == binding.marked) {
                        "Conflicting ForceNativeThreadState annotations for $name in ${previous?.file} and ${binding.file}; make them agree."
                    }
                    table[name] = binding
                }
                annotations.clear()
            } else if (line.trimStart().startsWith("@")) {
                annotations.append(line).append('\n')
                depth = code.count { it == '(' } - code.count { it == ')' }
            } else {
                annotations.clear()
            }
        }
    }
    return table
}
