package org.jetbrains.skia.skottie

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats

/**
 *
 * A Logger subclass can be used to receive
 * [org.jetbrains.skia.skottie.AnimationBuilder] parsing errors and warnings.
 */
abstract class Logger : RefCnt(_nMake()) {
    companion object {
        init {
            SkottieLibrary.load()
        }
    }

    abstract fun log(level: LogLevel, message: String, json: String?)

    init {
        Stats.onNativeCall()
        Stats.onNativeCall()
        doInit(nativePtr)
    }
}

internal expect fun Logger.doInit(ptr: NativePointer)

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nMake")
// No ForceNativeThreadState: creates a logger with empty strings and no callback, so it is short and cannot block.
// Native call path: SkikoLogger::SkikoLogger
private external fun _nMake(): NativePointer

// Native/JS only

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nInit")
// Needs ForceNativeThreadState: replacing the callback destroys the old one, which calls back into Kotlin to dispose
// it.
// Native call path: KVoidCallback::operator= -> ~KCallback -> disposeCallback
@ForceNativeThreadState
internal external fun Logger_nInit(ptr: NativePointer, onLog: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nGetLogMessage")
// No ForceNativeThreadState: returns the address of the stored log message string, so it is short and cannot block.
// Native call path: SkikoLogger::logMessage
internal external fun Logger_nGetLogMessage(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nGetLogJson")
// No ForceNativeThreadState: returns the address of the stored JSON log string, or null when there is none, so it is
// short and cannot block.
// Native call path: SkikoLogger::logJson
internal external fun Logger_nGetLogJson(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nGetLogLevel")
// No ForceNativeThreadState: returns the stored log level, so it is short and cannot block.
// Native call path: SkikoLogger::logLevel
internal external fun Logger_nGetLogLevel(ptr: NativePointer): Int
// </editor-fold>