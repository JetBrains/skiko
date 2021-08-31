package org.jetbrains.skija.skottie

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import org.jetbrains.skija.impl.Stats

/**
 *
 * A Logger subclass can be used to receive
 * [org.jetbrains.skija.skottie.AnimationBuilder] parsing errors and warnings.
 */
abstract class Logger : RefCnt(_nMake()) {
    companion object {
        @ApiStatus.Internal
        external fun _nMake(): Long

        init {
            staticLoad()
        }
    }

    @OverrideOnly
    abstract fun log(level: LogLevel?, message: String?, json: String?)
    @ApiStatus.Internal
    external fun _nInit(ptr: Long)

    init {
        Stats.onNativeCall()
        Stats.onNativeCall()
        _nInit(_ptr)
    }
}