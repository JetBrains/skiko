package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.nativeCall

class PixelRef internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }

    val width: Int
        get() = nativeCall { _nGetWidth(_ptr) }
    val height: Int
        get() = nativeCall { _nGetHeight(_ptr) }
    val rowBytes: Int
        get() = nativeCall { PixelRef_nGetRowBytes(_ptr) }
    val pixels: NativePointer
        get() = nativeCall { PixelRef_nGetPixels(_ptr) }

    /**
     * Returns a non-zero, unique value corresponding to the pixels in this
     * pixelref. Each time the pixels are changed (and notifyPixelsChanged is
     * called), a different generation ID will be returned.
     */
    val generationId: Int
        get() = nativeCall { PixelRef_nGetGenerationId(_ptr) }

    /**
     * Call this if you have changed the contents of the pixels. This will in-
     * turn cause a different generation ID value to be returned from
     * getGenerationID().
     */
    fun notifyPixelsChanged(): PixelRef = nativeCall {
        PixelRef_nNotifyPixelsChanged(_ptr)
        this
    }

    /**
     * Returns true if this pixelref is marked as immutable, meaning that the
     * contents of its pixels will not change for the lifetime of the pixelref.
     */
    val isImmutable: Boolean
        get() = nativeCall { PixelRef_nIsImmutable(_ptr) }

    /**
     * Marks this pixelref is immutable, meaning that the contents of its
     * pixels will not change for the lifetime of the pixelref. This state can
     * be set on a pixelref, but it cannot be cleared once it is set.
     */
    fun setImmutable(): PixelRef = nativeCall {
        PixelRef_nSetImmutable(_ptr)
        this
    }
}

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetPixels")
// No ForceNativeThreadState: returns the stored pixel address, so it is short and cannot block.
// Native call path: SkPixelRef::pixels
private external fun PixelRef_nGetPixels(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetRowBytes")
// No ForceNativeThreadState: returns the stored row byte count, so it is short and cannot block.
// Native call path: SkPixelRef::rowBytes
private external fun PixelRef_nGetRowBytes(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetGenerationId")
// No ForceNativeThreadState: reads a generation id with one atomic load or compare-exchange and takes no lock, so it is
// short and cannot block.
// Native call path: SkPixelRef::getGenerationID
private external fun PixelRef_nGetGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nNotifyPixelsChanged")
// Needs ForceNativeThreadState: walks every registered listener under a lock, so its time grows with the number of
// listeners.
// Native call path: SkPixelRef::notifyPixelsChanged -> SkIDChangeListener::List::changed
@ForceNativeThreadState
private external fun PixelRef_nNotifyPixelsChanged(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nIsImmutable")
// No ForceNativeThreadState: returns whether the pixels are immutable, so it is short and cannot block.
// Native call path: SkPixelRef::isImmutable
private external fun PixelRef_nIsImmutable(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nSetImmutable")
// No ForceNativeThreadState: marks the pixels immutable with a single field store, so it is short and cannot block.
// Native call path: SkPixelRef::setImmutable
private external fun PixelRef_nSetImmutable(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetWidth")
// No ForceNativeThreadState: returns the stored width, so it is short and cannot block.
// Native call path: SkPixelRef::width
private external fun _nGetWidth(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetHeight")
// No ForceNativeThreadState: returns the stored height, so it is short and cannot block.
// Native call path: SkPixelRef::height
private external fun _nGetHeight(ptr: NativePointer): Int
