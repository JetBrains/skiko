package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skiko.ExperimentalSkikoApi

/**
 * Provides the interface for recording drawing commands and snapping them into a [Recording].
 *
 * A recorder is intended to be used from one thread and is not thread-safe. Different recorders
 * can be used on different threads.
 */
@ExperimentalSkikoApi
class Recorder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    /**
     * Captures the drawing commands accumulated since the previous call to `snap`.
     *
     * The recorder is reset and ready to record new commands after this call.
     *
     * @return a recording containing the captured commands.
     */
    fun snap(): Recording {
        try {
            Stats.onNativeCall()
            val ptr = _nSnap(nativePtr)
            check(ptr != NullPointer) { "Failed to create a Graphite recording" }
            return Recording(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    private object _FinalizerHolder {
        val PTR = _nGetRecorderFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_Recorder__1nGetFinalizer")
// No ForceNativeThreadState: returns the function that frees the object, without calling it, so it is short and cannot
// block.
// Native call path: deleteRecorder
private external fun _nGetRecorderFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_Recorder__1nSnap")
// Needs ForceNativeThreadState: finishes pending drawing work and prepares resources through Metal.
// Native call path: Recorder::snap -> flushTrackedDevices -> prepareResources
@ForceNativeThreadState
private external fun _nSnap(recorderPtr: NativePointer): NativePointer
