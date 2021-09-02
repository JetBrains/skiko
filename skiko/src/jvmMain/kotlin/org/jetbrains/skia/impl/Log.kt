package org.jetbrains.skia.impl

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Supplier

object Log {
    private var _level = 0
    fun trace(s: String) {
        _log(1, "[TRACE]", s)
    }

    fun debug(s: String) {
        _log(2, "[DEBUG]", s)
    }

    fun info(s: String) {
        _log(3, "[INFO ]", s)
    }

    fun warn(s: String) {
        _log(4, "[WARN ]", s)
    }

    fun error(s: String) {
        _log(5, "[ERROR]", s)
    }

    fun trace(s: Supplier<String>) {
        _log(1, "[TRACE]", s)
    }

    fun debug(s: Supplier<String>) {
        _log(2, "[DEBUG]", s)
    }

    fun info(s: Supplier<String>) {
        _log(3, "[INFO ]", s)
    }

    fun warn(s: Supplier<String>) {
        _log(4, "[WARN ]", s)
    }

    fun error(s: Supplier<String>) {
        _log(5, "[ERROR]", s)
    }

    fun _time(): String {
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
    }

    fun _log(level: Int, prefix: String, s: String) {
        if (level >= _level) println(_time() + " " + prefix + " " + s)
    }

    fun _log(level: Int, prefix: String, s: Supplier<String>) {
        if (level >= _level) println(_time() + " " + prefix + " " + s.get())
    }

    init {
        val property = System.getProperty("skija.logLevel")
        _level =
            if ("ALL" == property)
                0 else if ("TRACE" == property)
                    1 else if ("DEBUG" == property)
                        2 else if ("INFO" == property)
                            3 else if (null == property || "WARN" == property)
                                4 else if ("ERROR" == property)
                                    5 else if ("NONE" == property)
                                        6 else
                                            throw IllegalArgumentException("Unknown log level: $property")
    }
}