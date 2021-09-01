package org.jetbrains.skija.skottie

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.*
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class AnimationBuilder internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        internal fun _flagsToInt(vararg builderFlags: AnimationBuilderFlag): Int {
            var flags = 0
            for (flag in builderFlags) flags = flags or flag._flag
            return flags
        }

        @JvmStatic external fun _nGetFinalizer(): Long
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
            _nSetFontManager(_ptr, Native.Companion.getPtr(fontMgr))
            this
        } finally {
            Reference.reachabilityFence(fontMgr)
        }
    }

    /**
     *
     * Register a [Logger] with this builder.
     */
    fun setLogger(logger: Logger?): AnimationBuilder {
        return try {
            Stats.onNativeCall()
            _nSetLogger(_ptr, Native.Companion.getPtr(logger))
            this
        } finally {
            Reference.reachabilityFence(logger)
        }
    }

    fun buildFromString(data: String): Animation {
        return try {
            assert(data != null) { "Can’t buildFromString with data == null" }
            Stats.onNativeCall()
            val ptr = _nBuildFromString(_ptr, data)
            require(ptr != 0L) { "Failed to create Animation from string: \"$data\"" }
            Animation(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun buildFromFile(path: String): Animation {
        return try {
            assert(path != null) { "Can’t buildFromFile with path == null" }
            Stats.onNativeCall()
            val ptr = _nBuildFromFile(_ptr, path)
            require(ptr != 0L) { "Failed to create Animation from path: $path" }
            Animation(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun buildFromData(data: Data): Animation {
        return try {
            assert(data != null) { "Can’t buildFromData with data == null" }
            Stats.onNativeCall()
            val ptr =
                _nBuildFromData(_ptr, Native.Companion.getPtr(data))
            require(ptr != 0L) { "Failed to create Animation from data" }
            Animation(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }
}