package org.jetbrains.skia.gpu.ganesh

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.GaneshLibrary
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

fun Bitmap.Companion.makeFromImage(image: Image, context: DirectContext): Bitmap {
    val bitmap = Bitmap()
    bitmap.allocPixels(image.imageInfo)
    return if (image.readPixels(context, bitmap)) bitmap else {
        bitmap.close()
        throw RuntimeException("Failed to readPixels from $image")
    }
}

/**
 * Returns the recording context being used by this Canvas.
 *
 * The returned context is borrowed from the Canvas and must not be closed.
 *
 * @return the recording context, if available; null otherwise
 */
val Canvas.recordingContext: DirectContext?
    get() = try {
        GaneshLibrary.load()
        Stats.onNativeCall()
        val ptr = _nGetCanvasRecordingContext(getPtr(this))
        if (ptr == NullPointer) null else DirectContext(ptr, managed = false)
    } finally {
        reachabilityBarrier(this)
    }

/**
 *
 * Returns the recording context being used by the Surface.
 *
 * The returned context is borrowed from the Surface and must not be closed.
 *
 * @return the recording context, if available; null otherwise
 */
val Surface.recordingContext: DirectContext?
    get() = try {
        GaneshLibrary.load()
        Stats.onNativeCall()
        val ptr = _nGetSurfaceRecordingContext(getPtr(this))
        if (ptr == NullPointer) null else DirectContext(ptr, managed = false)
    } finally {
        reachabilityBarrier(this)
    }

/**
 *
 * Call to ensure all reads/writes of the surface have been issued to the underlying 3D API.
 *
 *
 * Skia will correctly order its own draws and pixel operations.
 * This must to be used to ensure correct ordering when the surface backing store is accessed
 * outside Skia (e.g. direct use of the 3D API or a windowing system).
 * DirectContext has additional flush and submit methods that apply to all surfaces and images created from
 * a DirectContext.
 */
fun Surface.flushAndSubmit() {
    recordingContext?.flushAndSubmit(this)
}

/**
 *
 * Call to ensure all reads/writes of the surface have been issued to the underlying 3D API.
 *
 *
 * Skia will correctly order its own draws and pixel operations.
 * This must to be used to ensure correct ordering when the surface backing store is accessed
 * outside Skia (e.g. direct use of the 3D API or a windowing system).
 * DirectContext has additional flush and submit methods that apply to all surfaces and images created from
 * a DirectContext.
 *
 * @param syncCpu a flag determining if cpu should be synced
 */
fun Surface.flushAndSubmit(syncCpu: Boolean) {
    recordingContext?.flushAndSubmit(this, syncCpu)
}

fun Surface.flush() {
    recordingContext?.flush(this)
}

@ExternalSymbolName("org_jetbrains_skia_gpu_ganesh_ContextExtensions__1nGetCanvasRecordingContext")
private external fun _nGetCanvasRecordingContext(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_ganesh_ContextExtensions__1nGetSurfaceRecordingContext")
private external fun _nGetSurfaceRecordingContext(ptr: NativePointer): NativePointer
