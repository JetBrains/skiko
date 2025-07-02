@file:Suppress("NESTED_EXTERNAL_DECLARATION")

package org.jetbrains.skia.skottie

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats

/**
 *
 * A Logger subclass can be used to receive
 * [org.jetbrains.skia.skottie.AnimationBuilder] parsing errors and warnings.
 */
abstract class Logger : RefCnt(Logger_nMake()) {
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