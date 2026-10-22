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

// Runnable: new SkikoLogger default-constructs empty SkStrings and a KVoidCallback holding nullptr
@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nMake")
private external fun _nMake(): NativePointer

// Native/JS only

// Native: assigning KVoidCallback destroys the old one -> ~KCallback -> disposeCallback into Kotlin
@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nInit")
@ForceNativeThreadState
internal external fun Logger_nInit(ptr: NativePointer, onLog: InteropPointer)

// Runnable: SkikoLogger::logMessage returns the address of the stored SkString
@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nGetLogMessage")
internal external fun Logger_nGetLogMessage(ptr: NativePointer): NativePointer

// Runnable: SkikoLogger::logJson returns the address of a stored SkString or null
@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nGetLogJson")
internal external fun Logger_nGetLogJson(ptr: NativePointer): NativePointer

// Runnable: SkikoLogger::logLevel returns the stored level field
@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nGetLogLevel")
internal external fun Logger_nGetLogLevel(ptr: NativePointer): Int
