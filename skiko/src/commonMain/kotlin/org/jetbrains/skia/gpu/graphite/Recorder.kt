package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer

@ExperimentalSkikoApi
class Recorder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    fun snap(): Recording {
        return Recording(Recorder_nSnap(_ptr))
    }

    private object _FinalizerHolder {
        val PTR = Recorder_nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_Recorder__1nGetFinalizer")
private external fun Recorder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_Recorder__1nSnap")
private external fun Recorder_nSnap(recorderPtr: NativePointer): NativePointer