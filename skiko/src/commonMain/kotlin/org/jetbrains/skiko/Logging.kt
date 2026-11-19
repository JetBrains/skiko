package org.jetbrains.skiko

import org.jetbrains.skiko.internal.Logger
import kotlin.time.Duration.Companion.nanoseconds

interface SkikoLoggerInterface {
    val isTraceEnabled: Boolean
    val isDebugEnabled: Boolean
    val isInfoEnabled: Boolean
    val isWarnEnabled: Boolean
    val isErrorEnabled: Boolean

    fun trace(message: String)
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)

    fun trace(t: Throwable, message: String)
    fun debug(t: Throwable, message: String)
    fun info(t: Throwable, message: String)
    fun warn(t: Throwable, message: String)
    fun error(t: Throwable, message: String)
}

fun setupSkikoLoggerFactory(createLogger: () -> SkikoLoggerInterface) {
    Logger.loggerFactory = createLogger
}

internal enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    fun noMoreVerboseThan(other: LogLevel): Boolean {
        return this.ordinal >= other.ordinal
    }
}

class DefaultConsoleLogger(
    override val isTraceEnabled: Boolean = false,
    override val isDebugEnabled: Boolean = false,
    override val isInfoEnabled: Boolean = true,
    override val isWarnEnabled: Boolean = true,
    override val isErrorEnabled: Boolean = true,
    private val logTimestamp: Boolean = false
): SkikoLoggerInterface {

    companion object {
        fun fromLevel(level: String): DefaultConsoleLogger {
            val logLevel = LogLevel.entries.firstOrNull { it.name == level } ?: LogLevel.INFO
            return DefaultConsoleLogger(
                isTraceEnabled = LogLevel.TRACE.noMoreVerboseThan(logLevel),
                isDebugEnabled = LogLevel.DEBUG.noMoreVerboseThan(logLevel),
                isInfoEnabled = LogLevel.INFO.noMoreVerboseThan(logLevel),
                isWarnEnabled = LogLevel.WARN.noMoreVerboseThan(logLevel),
                isErrorEnabled = LogLevel.ERROR.noMoreVerboseThan(logLevel)
            )
        }
    }

    private fun logMessagePrefix() = if (logTimestamp) {
        "[${currentNanoTime().nanoseconds.inWholeMilliseconds.mod(1_000)}]"
    } else "[SKIKO]"

    override fun trace(message: String) {
        println("${logMessagePrefix()} trace: $message")
    }

    override fun trace(t: Throwable, message: String) {
        println("${logMessagePrefix()} trace: $message")
        println(t)
    }

    override fun debug(message: String) {
        println("${logMessagePrefix()} debug: $message")
    }

    override fun debug(t: Throwable, message: String) {
        println("${logMessagePrefix()} debug: $message")
        println(t)
    }

    override fun info(message: String) {
        println("${logMessagePrefix()} info: $message")
    }

    override fun info(t: Throwable, message: String) {
        println("${logMessagePrefix()} info: $message")
        println(t)
    }

    override fun warn(message: String) {
        println("${logMessagePrefix()} warn: $message")
    }

    override fun warn(t: Throwable, message: String) {
        println("${logMessagePrefix()} warn: $message")
        println(t)
    }

    override fun error(message: String) {
        println("${logMessagePrefix()} error: $message")
    }

    override fun error(t: Throwable, message: String) {
        println("${logMessagePrefix()} error: $message")
        println(t)
    }
}
