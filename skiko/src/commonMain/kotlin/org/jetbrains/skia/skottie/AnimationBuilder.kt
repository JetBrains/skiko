@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.skottie

import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

class AnimationBuilder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        internal fun _flagsToInt(vararg builderFlags: AnimationBuilderFlag): Int {
            var flags = 0
            for (flag in builderFlags) flags = flags or flag._flag
            return flags
        }

        init {
            staticLoad()
        }
    }

    private object _FinalizerHolder {
        val PTR = AnimationBuilder_nGetFinalizer()
    }

    constructor() : this(*emptyArray<AnimationBuilderFlag>()) {}
    constructor(vararg builderFlags: AnimationBuilderFlag) : this(AnimationBuilder_nMake(_flagsToInt(*builderFlags))) {
        Stats.onNativeCall()
    }

    /**
     *
     * Specify a font manager for loading animation fonts.
     */
    fun setFontManager(fontMgr: FontMgr?): AnimationBuilder {
        return try {
            Stats.onNativeCall()
            AnimationBuilder_nSetFontManager(_ptr, getPtr(fontMgr))
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(fontMgr)
        }
    }

    /**
     *
     * Register a [Logger] with this builder.
     */
    fun setLogger(logger: Logger?): AnimationBuilder {
        return try {
            Stats.onNativeCall()
            AnimationBuilder_nSetLogger(_ptr, getPtr(logger))
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(logger)
        }
    }

    fun buildFromString(data: String): Animation {
        return try {
            Stats.onNativeCall()
            val ptr = interopScope { AnimationBuilder_nBuildFromString(_ptr, toInterop(data)) }
            require(ptr != NullPointer) { "Failed to create Animation from string: \"$data\"" }
            Animation(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun buildFromData(data: Data): Animation {
        return try {
            Stats.onNativeCall()
            val ptr =
                AnimationBuilder_nBuildFromData(_ptr, getPtr(data))
            require(ptr != NullPointer) { "Failed to create Animation from data" }
            Animation(ptr)
        } finally {
            reachabilityBarrier(data)
            reachabilityBarrier(this)
        }
    }
}