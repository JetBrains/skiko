package org.jetbrains.skiko

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

class DefaultConsoleLogger(override val isTraceEnabled: Boolean = true,
                           override val isDebugEnabled: Boolean = true,
                           override val isInfoEnabled: Boolean = true,
                           override val isWarnEnabled: Boolean = true,
                           override val isErrorEnabled: Boolean = true): SkikoLoggerInterface {

    companion object {
        fun fromLevel(level: String): DefaultConsoleLogger {
            val logLevel = LogLevel.values().filter { it.name == level }.firstOrNull() ?: LogLevel.INFO
            return DefaultConsoleLogger(
                isTraceEnabled = LogLevel.TRACE.noMoreVerboseThan(logLevel),
                isDebugEnabled = LogLevel.DEBUG.noMoreVerboseThan(logLevel),
                isInfoEnabled = LogLevel.INFO.noMoreVerboseThan(logLevel),
                isWarnEnabled = LogLevel.WARN.noMoreVerboseThan(logLevel),
                isErrorEnabled = LogLevel.ERROR.noMoreVerboseThan(logLevel)
            )
        }
    }

    override fun trace(message: String) {
        append("[SKIKO] trace: $message")
    }

    override fun trace(t: Throwable, message: String) {
        append("[SKIKO] trace: $message")
        append(t.toString())
    }

    override fun debug(message: String) {
        append("[SKIKO] debug: $message")
    }

    override fun debug(t: Throwable, message: String) {
        append("[SKIKO] debug: $message")
        append(t.toString())
    }

    override fun info(message: String) {
        append("[SKIKO] info: $message")
    }

    override fun info(t: Throwable, message: String) {
        append("[SKIKO] info: $message")
        append(t.toString())
    }

    override fun warn(message: String) {
        append("[SKIKO] warn: $message")
    }

    override fun warn(t: Throwable, message: String) {
        append("[SKIKO] warn: $message")
        append(t.toString())
    }

    override fun error(message: String) {
        append("[SKIKO] error: $message")
    }

    override fun error(t: Throwable, message: String) {
        append("[SKIKO] error: $message")
        append(t.toString())
    }

    fun append(msg: String) {
        java.io.File("log.txt").appendText("$msg\n")
    }
}

internal object Logger {
    var loggerFactory: () -> SkikoLoggerInterface = { DefaultConsoleLogger() }

    val loggerImpl by lazy {
        loggerFactory()
    }

    inline fun trace(msg: () -> String) {
        if (loggerImpl.isTraceEnabled) {
            loggerImpl.trace(msg())
        }
    }

    inline fun debug(msg: () -> String) {
        if (loggerImpl.isDebugEnabled) {
            loggerImpl.debug(msg())
        }
    }

    inline fun info(msg: () -> String) {
        if (loggerImpl.isInfoEnabled) {
            loggerImpl.info(msg())
        }
    }

    inline fun warn(msg: () -> String) {
        if (loggerImpl.isWarnEnabled) {
            loggerImpl.warn(msg())
        }
    }

    inline fun error(msg: () -> String) {
        if (loggerImpl.isErrorEnabled) {
            loggerImpl.error(msg())
        }
    }

    inline fun trace(t: Throwable, msg: () ->  String) {
        if (loggerImpl.isTraceEnabled) {
            loggerImpl.trace(t, msg())
        }
    }

    inline fun debug(t: Throwable, msg: () ->  String) {
        if (loggerImpl.isDebugEnabled) {
            loggerImpl.debug(t, msg())
        }
    }

    inline fun info(t: Throwable, msg: () -> String) {
        if (loggerImpl.isInfoEnabled) {
            loggerImpl.info(t, msg())
        }
    }
    inline fun warn(t: Throwable, msg: () -> String) {
        if (loggerImpl.isWarnEnabled) {
            loggerImpl.warn(t, msg())
        }
    }
    inline fun error(t: Throwable, msg:() ->  String) {
        if (loggerImpl.isErrorEnabled) {
            loggerImpl.error(t, msg())
        }
    }
}
