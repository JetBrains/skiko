package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi

/**
 * An immutable collection of drawing commands snapped from a [Recorder] for GPU submission.
 */
@ExperimentalSkikoApi
class Recording internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    private object _FinalizerHolder {
        val PTR = _nGetRecordingFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_Recording__1nGetFinalizer")
// No ForceNativeThreadState: returns the function that frees the object, without calling it, so it is short and cannot
// block.
// Native call path: deleteRecording
private external fun _nGetRecordingFinalizer(): NativePointer
