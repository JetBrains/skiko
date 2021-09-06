@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.skottie

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

class AnimationBuilder internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        internal fun _flagsToInt(vararg builderFlags: AnimationBuilderFlag): Int {
            var flags = 0
            for (flag in builderFlags) flags = flags or flag._flag
            return flags
        }

        @JvmStatic
        external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nMake(flags: Int): Long
        @JvmStatic external fun _nSetFontManager(ptr: Long, fontMgrPtr: Long)
        @JvmStatic external fun _nSetLogger(ptr: Long, loggerPtr: Long)
        @JvmStatic external fun _nBuildFromString(ptr: Long, data: String?): Long
        @JvmStatic external fun _nBuildFromFile(ptr: Long, path: String?): Long
        @JvmStatic external fun _nBuildFromData(ptr: Long, dataPtr: Long): Long

        init {
            staticLoad()
        }
    }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    constructor() : this(*emptyArray<AnimationBuilderFlag>()) {}
    constructor(vararg builderFlags: AnimationBuilderFlag) : this(_nMake(_flagsToInt(*builderFlags))) {
        Stats.onNativeCall()
    }

    /**
     *
     * Specify a font manager for loading animation fonts.
     */
    fun setFontManager(fontMgr: FontMgr?): AnimationBuilder {
        return try {
            Stats.onNativeCall()
            _nSetFontManager(_ptr, getPtr(fontMgr))
            this
        } finally {
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
            _nSetLogger(_ptr, getPtr(logger))
            this
        } finally {
            reachabilityBarrier(logger)
        }
    }

    fun buildFromString(data: String): Animation {
        return try {
            Stats.onNativeCall()
            val ptr = _nBuildFromString(_ptr, data)
            require(ptr != 0L) { "Failed to create Animation from string: \"$data\"" }
            Animation(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun buildFromFile(path: String): Animation {
        return try {
            Stats.onNativeCall()
            val ptr = _nBuildFromFile(_ptr, path)
            require(ptr != 0L) { "Failed to create Animation from path: $path" }
            Animation(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun buildFromData(data: Data): Animation {
        return try {
            Stats.onNativeCall()
            val ptr =
                _nBuildFromData(_ptr, getPtr(data))
            require(ptr != 0L) { "Failed to create Animation from data" }
            Animation(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }
}