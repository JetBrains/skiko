package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skiko.ExperimentalSkikoApi

@ExperimentalSkikoApi
class Recorder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
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
private external fun _nGetRecorderFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_Recorder__1nSnap")
private external fun _nSnap(recorderPtr: NativePointer): NativePointer
