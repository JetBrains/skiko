package org.jetbrains.skia.skottie

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats

/**
 *
 * A Logger subclass can be used to receive
 * [org.jetbrains.skia.skottie.AnimationBuilder] parsing errors and warnings.
 */
abstract class Logger : RefCnt(_nMake()) {
    companion object {
        @JvmStatic external fun _nMake(): Long

        init {
            staticLoad()
        }
    }

    abstract fun log(level: LogLevel?, message: String?, json: String?)
    external fun _nInit(ptr: Long)

    init {
        Stats.onNativeCall()
        Stats.onNativeCall()
        _nInit(_ptr)
    }
}