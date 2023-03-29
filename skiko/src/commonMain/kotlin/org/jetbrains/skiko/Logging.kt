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

internal object DefaultConsoleLogger: SkikoLoggerInterface {
    override val isTraceEnabled: Boolean
        get() = false
    override val isDebugEnabled: Boolean
        get() = false
    override val isInfoEnabled: Boolean
        get() = true
    override val isWarnEnabled: Boolean
        get() = true
    override val isErrorEnabled: Boolean
        get() = true

    override fun trace(message: String) {
        println("[SKIKO] trace: $message")
    }

    override fun trace(t: Throwable, message: String) {
        println("[SKIKO] trace: $message")
        println(t)
    }

    override fun debug(message: String) {
        println("[SKIKO] debug: $message")
    }

    override fun debug(t: Throwable, message: String) {
        println("[SKIKO] debug: $message")
        println(t)
    }

    override fun info(message: String) {
        println("[SKIKO] info: $message")
    }

    override fun info(t: Throwable, message: String) {
        println("[SKIKO] info: $message")
        println(t)
    }

    override fun warn(message: String) {
        println("[SKIKO] warn: $message")
    }

    override fun warn(t: Throwable, message: String) {
        println("[SKIKO] warn: $message")
        println(t)
    }

    override fun error(message: String) {
        println("[SKIKO] error: $message")
    }

    override fun error(t: Throwable, message: String) {
        println("[SKIKO] error: $message")
        println(t)
    }
}

internal object Logger {
    var loggerFactory: () -> SkikoLoggerInterface = { DefaultConsoleLogger }

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
