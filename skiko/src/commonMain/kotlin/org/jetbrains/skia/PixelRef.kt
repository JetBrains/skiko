package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer

class PixelRef internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
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
            PixelRef_nGetRowBytes(_ptr)
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
            PixelRef_nGetGenerationId(_ptr)
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
        PixelRef_nNotifyPixelsChanged(_ptr)
        return this
    }

    /**
     * Returns true if this pixelref is marked as immutable, meaning that the
     * contents of its pixels will not change for the lifetime of the pixelref.
     */
    val isImmutable: Boolean
        get() = try {
            Stats.onNativeCall()
            PixelRef_nIsImmutable(_ptr)
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
        PixelRef_nSetImmutable(_ptr)
        return this
    }
}

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetRowBytes")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PixelRef__1nGetRowBytes")
private external fun PixelRef_nGetRowBytes(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetGenerationId")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PixelRef__1nGetGenerationId")
private external fun PixelRef_nGetGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nNotifyPixelsChanged")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PixelRef__1nNotifyPixelsChanged")
private external fun PixelRef_nNotifyPixelsChanged(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nIsImmutable")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PixelRef__1nIsImmutable")
private external fun PixelRef_nIsImmutable(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nSetImmutable")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PixelRef__1nSetImmutable")
private external fun PixelRef_nSetImmutable(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PixelRef__1nGetWidth")
private external fun _nGetWidth(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PixelRef__1nGetHeight")
private external fun _nGetHeight(ptr: NativePointer): Int