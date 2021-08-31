package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class PixelRef @ApiStatus.Internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        @ApiStatus.Internal
        external fun _nGetWidth(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nGetHeight(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nGetRowBytes(ptr: Long): Long
        @ApiStatus.Internal
        external fun _nGetGenerationId(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nNotifyPixelsChanged(ptr: Long)
        @ApiStatus.Internal
        external fun _nIsImmutable(ptr: Long): Boolean
        @ApiStatus.Internal
        external fun _nSetImmutable(ptr: Long)

        init {
            staticLoad()
        }
    }

    val width: Int
        get() = try {
            Stats.onNativeCall()
            _nGetWidth(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val height: Int
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val rowBytes: Long
        get() = try {
            Stats.onNativeCall()
            _nGetRowBytes(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * Returns a non-zero, unique value corresponding to the pixels in this
     * pixelref. Each time the pixels are changed (and notifyPixelsChanged is
     * called), a different generation ID will be returned.
     */
    val generationId: Int
        get() = try {
            Stats.onNativeCall()
            _nGetGenerationId(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * Call this if you have changed the contents of the pixels. This will in-
     * turn cause a different generation ID value to be returned from
     * getGenerationID().
     */
    fun notifyPixelsChanged(): PixelRef {
        Stats.onNativeCall()
        _nNotifyPixelsChanged(_ptr)
        return this
    }

    /**
     * Returns true if this pixelref is marked as immutable, meaning that the
     * contents of its pixels will not change for the lifetime of the pixelref.
     */
    val isImmutable: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsImmutable(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * Marks this pixelref is immutable, meaning that the contents of its
     * pixels will not change for the lifetime of the pixelref. This state can
     * be set on a pixelref, but it cannot be cleared once it is set.
     */
    fun setImmutable(): PixelRef {
        Stats.onNativeCall()
        _nSetImmutable(_ptr)
        return this
    }
}