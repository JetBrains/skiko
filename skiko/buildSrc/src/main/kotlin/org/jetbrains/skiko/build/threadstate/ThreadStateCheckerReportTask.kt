package org.jetbrains.skiko.build.threadstate

import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Instant
import javax.inject.Inject
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

private data class BlockingCall(val binding: String, val name: String, val count: Long, val frames: List<String>)
private data class CollectionData(val loadAddress: String, val called: Set<String>, val calls: List<BlockingCall>)
private data class ReportCase(
    val name: String,
    val className: String,
    val detail: String,
    val failureType: String? = null,
    val message: String? = null
)

abstract class ThreadStateCheckerReportTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {
    @get:Input
    abstract val targetName: Property<String>

    @get:Internal
    abstract val executableFile: RegularFileProperty

    @get:Internal
    abstract val bindingsFile: RegularFileProperty

    @get:Internal
    abstract val logFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun report() {
        val started = System.nanoTime()
        val cases = try {
            val bindings = readBindings(bindingsFile.get().asFile)
            val data = readCollection(logFile.get().asFile, bindings)
            val grouped = data.calls.groupBy { it.binding }
            val symbols = symbolize(executableFile.get().asFile, data.loadAddress, data.calls.flatMap { it.frames }.distinct())
            data.called.sorted().map { name ->
                val binding = bindings.getValue(name)
                val blocking = grouped[name].orEmpty()
                val detail = buildString {
                    append("Binding: $name\nSource: ${binding.file}\n")
                    append("Blocking calls: ${blocking.sumOf { it.count }}\n")
                    for (call in blocking.sortedBy { it.name }) {
                        append("${call.name}: ${call.count} calls\nSample stack:\n")
                        call.frames.forEach { append(symbols.getValue(it)).append('\n') }
                    }
                    if (binding.marked && blocking.isEmpty()) {
                        append("No blocking call observed. ForceNativeThreadState also covers CPU work and callbacks, which this checker cannot observe.")
                    }
                }
                val mismatch = !binding.marked && blocking.isNotEmpty()
                ReportCase(
                    name, binding.file.substringAfter("kotlin/").removeSuffix(".kt").replace('/', '.'), detail,
                    if (mismatch) "ThreadStateMismatch" else null,
                    if (mismatch) "Unmarked binding made a blocking call" else null
                )
            }
        } catch (e: Exception) {
            listOf(ReportCase(
                "collection", "org.jetbrains.skiko.ThreadStateChecker",
                "${e.message ?: e.javaClass.simpleName}. Rerun the instrumented test task and check its output.",
                "IncompleteData", "Collection is incomplete"
            ))
        }
        val output = outputFile.get().asFile
        writeReport(output, cases, started)
        val failures = cases.filter { it.failureType != null }
        logger.lifecycle("Thread-state checker ${targetName.get()}: ${failures.size} failures. JUnit XML: ${output.absolutePath}")
        if (failures.isNotEmpty()) {
            throw GradleException(
                "Thread-state checker failed: " +
                    failures.groupingBy { it.failureType }.eachCount().entries.joinToString { "${it.key}=${it.value}" } +
                    ". First failing bindings (up to 20): " +
                    failures.filter { it.name != "collection" }.take(20).joinToString { it.name }.ifEmpty { "none" } +
                    ". JUnit XML: ${output.absolutePath}"
            )
        }
    }

    private fun writeReport(output: File, cases: List<ReportCase>, started: Long) {
        output.parentFile.mkdirs()
        output.outputStream().buffered().use { stream ->
            val xml = XMLOutputFactory.newFactory().createXMLStreamWriter(stream, "UTF-8")
            try {
                xml.writeStartDocument("UTF-8", "1.0")
                xml.writeStartElement("testsuite")
                xml.writeAttribute("name", "org.jetbrains.skiko.ThreadStateChecker")
                xml.writeAttribute("tests", cases.size.toString())
                xml.writeAttribute("failures", cases.count { it.failureType != null }.toString())
                xml.writeAttribute("skipped", "0")
                xml.writeAttribute("errors", "0")
                xml.writeAttribute("timestamp", Instant.now().toString())
                xml.writeAttribute("time", ((System.nanoTime() - started) / 1_000_000_000.0).toString())
                for (case in cases) {
                    xml.writeStartElement("testcase")
                    xml.writeAttribute("name", case.name)
                    xml.writeAttribute("classname", case.className)
                    xml.writeAttribute("time", "0.0")
                    if (case.failureType != null) {
                        xml.writeStartElement("failure")
                        xml.writeAttribute("type", case.failureType)
                        xml.writeAttribute("message", case.message)
                    } else {
                        xml.writeStartElement("system-out")
                    }
                    xml.writeXmlText(case.detail)
                    xml.writeEndElement()
                    xml.writeEndElement()
                }
                xml.writeEndElement()
                xml.writeEndDocument()
                xml.flush()
            } finally {
                xml.close()
            }
        }
    }

    private fun symbolize(executable: File, loadAddress: String, frames: List<String>): Map<String, String> {
        val symbols = mutableMapOf<String, String>()
        for (chunk in frames.chunked(200)) {
            val stdout = ByteArrayOutputStream()
            val stderr = ByteArrayOutputStream()
            try {
                val result = execOperations.exec {
                    commandLine(listOf("xcrun", "atos", "-o", executable.absolutePath, "-l", loadAddress) + chunk.map {
                        "0x" + (it.removePrefix("0x").toULong(16) - 1uL).toString(16)
                    })
                    standardOutput = stdout
                    errorOutput = stderr
                    isIgnoreExitValue = true
                }
                check(result.exitValue == 0) { stderr.toString(Charsets.UTF_8.name()) }
                val resolved = stdout.toString(Charsets.UTF_8.name()).lineSequence().toList()
                check(resolved.size >= chunk.size) { "atos returned fewer symbols than requested" }
                chunk.zip(resolved).forEach { (address, symbol) -> symbols[address] = "$address $symbol" }
            } catch (e: Exception) {
                throw GradleException("Stack symbolization failed for $executable. Check that Xcode command-line tools and the test executable's debug symbols are available, then rerun the report. ${stderr.toString(Charsets.UTF_8.name())}", e)
            }
        }
        return symbols
    }
}

private fun XMLStreamWriter.writeXmlText(text: String) {
    writeCharacters(text.map { character ->
        if ((character < ' ' && character !in "\n\r\t") || character == '\uFFFE' || character == '\uFFFF') {
            '\uFFFD'
        } else {
            character
        }
    }.joinToString(""))
}

internal fun readBindings(file: File): Map<String, Binding> {
    val bindings = linkedMapOf<String, Binding>()
    file.forEachLine { line ->
        val fields = line.split(' ', limit = 4)
        require(fields.size == 4 && isAddress(fields[0]) && fields[1].isNotBlank() &&
            fields[2] in setOf("0", "1") && fields[3].isNotBlank()) { "Malformed binding: $line" }
        require(bindings.put(fields[1], Binding(fields[2] == "1", fields[3])) == null) {
            "Duplicate binding: ${fields[1]}"
        }
    }
    return bindings
}

private fun isAddress(text: String): Boolean =
    text.matches(Regex("(?:0x)?[0-9a-fA-F]+")) && text.removePrefix("0x").toULongOrNull(16) != null

private fun readCollection(file: File, bindings: Map<String, Binding>): CollectionData {
    val lines = file.readLines()
    val end = lines.lastOrNull()?.let { Regex("# end ([0-9]+)").matchEntire(it) }
    require(end != null && end.groupValues[1].toLongOrNull() == (lines.size - 1).toLong()) {
        "Missing or incorrect end record in $file"
    }
    val called = mutableSetOf<String>()
    val calls = mutableListOf<BlockingCall>()
    var loadAddress: String? = null
    var dropped: Long? = null
    for (line in lines.dropLast(1)) {
        val fields = line.split('\t')
        fun malformed(): Nothing = throw IllegalArgumentException("Malformed or unknown record in $file: $line")
        when (fields.first()) {
            "header" -> {
                if (fields.size != 2 || loadAddress != null || !isAddress(fields[1])) malformed()
                loadAddress = fields[1]
            }
            "dropped" -> {
                if (fields.size != 2 || dropped != null || !fields[1].matches(Regex("[0-9]+"))) malformed()
                dropped = fields[1].toLongOrNull() ?: malformed()
            }
            "called" -> {
                if (fields.size != 2 || fields[1] !in bindings || !called.add(fields[1])) malformed()
            }
            "call" -> {
                if (fields.size < 4 || fields[1] !in bindings || fields[2].isBlank() ||
                    !fields[3].matches(Regex("[0-9]+")) || fields.drop(4).any { !isAddress(it) }) malformed()
                val count = fields[3].toLongOrNull()?.takeIf { it > 0 } ?: malformed()
                calls.add(BlockingCall(fields[1], fields[2], count, fields.drop(4)))
            }
            "error" -> throw IllegalArgumentException("Checker collection failed: ${fields.drop(1).joinToString(" ")}")
            else -> malformed()
        }
    }
    require(loadAddress != null) { "Missing executable load address in $file" }
    require(dropped != null) { "Missing dropped counter in $file" }
    require(dropped == 0L) { "Collection dropped $dropped records in $file" }
    require(calls.all { it.binding in called }) { "Blocking call without a called record in $file" }
    return CollectionData(loadAddress, called, calls)
}
