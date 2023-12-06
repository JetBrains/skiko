@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.skottie

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Library.Companion.staticLoad
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
            staticLoad()
        }
    }

    abstract fun log(level: LogLevel, message: String, json: String?)

    init {
        Stats.onNativeCall()
        Stats.onNativeCall()
        doInit(_ptr)
    }
}

internal expect fun Logger.doInit(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_skottie_Logger__1nMake")
private external fun _nMake(): NativePointer

// Native/JS only

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nInit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_skottie_Logger__1nInit")
internal external fun Logger_nInit(ptr: NativePointer, onLog: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nGetLogMessage")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_skottie_Logger__1nGetLogMessage")
internal external fun Logger_nGetLogMessage(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nGetLogJson")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_skottie_Logger__1nGetLogJson")
internal external fun Logger_nGetLogJson(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nGetLogLevel")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_skottie_Logger__1nGetLogLevel")
internal external fun Logger_nGetLogLevel(ptr: NativePointer): Int
