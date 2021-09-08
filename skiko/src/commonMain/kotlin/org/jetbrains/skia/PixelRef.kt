@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import kotlin.jvm.JvmStatic

class PixelRef internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetWidth")
        external fun _nGetWidth(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetHeight")
        external fun _nGetHeight(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetRowBytes")
        external fun _nGetRowBytes(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetGenerationId")
        external fun _nGetGenerationId(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PixelRef__1nNotifyPixelsChanged")
        external fun _nNotifyPixelsChanged(ptr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PixelRef__1nIsImmutable")
        external fun _nIsImmutable(ptr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PixelRef__1nSetImmutable")
        external fun _nSetImmutable(ptr: NativePointer)

        init {
            staticLoad()
        }
    }

    val width: Int
        get() = try {
            Stats.onNativeCall()
            _nGetWidth(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val height: Int
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val rowBytes: NativePointer
        get() = try {
            Stats.onNativeCall()
            _nGetRowBytes(_ptr)
        } finally {
            reachabilityBarrier(this)
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
            reachabilityBarrier(this)
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
            reachabilityBarrier(this)
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