@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.skottie

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer

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

    abstract fun log(level: LogLevel?, message: String?, json: String?)

    init {
        Stats.onNativeCall()
        Stats.onNativeCall()
        _nInit(_ptr)
    }
}

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nMake")
private external fun _nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Logger__1nInit")
external fun _nInit(ptr: NativePointer)
