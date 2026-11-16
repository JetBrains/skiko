package org.jetbrains.skiko.internal

import org.jetbrains.skiko.DefaultConsoleLogger
import org.jetbrains.skiko.InternalSkikoApi
import org.jetbrains.skiko.SkikoLoggerInterface

@InternalSkikoApi
object Logger {
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
