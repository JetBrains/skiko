package internal.utils

import org.gradle.api.logging.Logger
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

internal abstract class RunExternalProcessWork: WorkAction<RunExternalProcessWorkParameters> {
    @get:Inject
    abstract val execOperations: ExecOperations

    override fun execute() {
        val log = BufferedLog()
        var failure: Exception? = null
        try {
            LineBufferingOutputStream { log.log(it) }.use { out ->
                LineBufferingOutputStream { log.error(it) }.use { err ->
                    execOperations.exec {
                        executable = parameters.executable
                        args = parameters.args
                        workingDir = parameters.workingDir
                        errorOutput = err
                        standardOutput = out
                    }.assertNormalExitValue()
                }
            }
        } catch (e: Exception) {
            failure = e
        }

        workResults[parameters.workId] = WorkResult(log = log, failure = failure)
        if (failure != null)
            throw failure
    }

    companion object {
        val workResults = ConcurrentHashMap<String, WorkResult>()
    }
}

internal class WorkResult(val log: BufferedLog, val failure: Exception?)

internal class BufferedLog() {
    private val logLines = arrayListOf<LogLine>()
    private data class LogLine(val line: String, val isError: Boolean)

    @Synchronized
    fun log(message: String) {
        logLines.add(LogLine(message, isError = false))
    }

    @Synchronized
    fun error(message: String) {
        logLines.add(LogLine(message, isError = false))
    }

    @Synchronized
    fun flushTo(logger: Logger) {
        for ((line, isError) in logLines) {
            val lineWithIndent = "  > $line"
            if (isError) logger.error(lineWithIndent) else logger.warn(lineWithIndent)
        }
    }
}

internal class LineBufferingOutputStream(private val processLine: (String) -> Unit) : OutputStream() {
    private val newLine = '\n'.toInt()
    private val buffer = ByteArrayOutputStream()
    private var closed = false

    override fun write(b: Int) {
        if (b == newLine) {
            flushBuffer()
        } else {
            buffer.write(b)
        }
    }

    private fun flushBuffer() {
        if (buffer.size() > 0) {
            val s = buffer.toString(Charsets.UTF_8)
            buffer.reset()
            processLine(s)
        }
    }

    override fun close() {
        flushBuffer()
        closed = true
    }

    override fun flush() {
        flushBuffer()
    }
}