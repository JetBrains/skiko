package org.jetbrains.skia.skottie

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.withStringReferenceNullableResult
import org.jetbrains.skia.impl.withStringReferenceResult

internal actual fun Logger.doInit(ptr: NativePointer) {
    interopScope {
        val onLog = virtual {
            val level = LogLevel.entries[Logger_nGetLogLevel(ptr)]
            val message = withStringReferenceResult { Logger_nGetLogMessage(ptr) }
            val json = withStringReferenceNullableResult { Logger_nGetLogJson(ptr) }
            log(level, message, json)
        }
        Logger_nInit(ptr, onLog)
    }
}
