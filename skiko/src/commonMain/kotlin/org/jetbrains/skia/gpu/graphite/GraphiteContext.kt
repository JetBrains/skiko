package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skiko.ExperimentalSkikoApi

@ExperimentalSkikoApi
class GraphiteContext internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        fun makeMetal(devicePtr: NativePointer, queuePtr: NativePointer): GraphiteContext {
            Stats.onNativeCall()
            return GraphiteContext(GraphiteContext_nMakeMetal(devicePtr, queuePtr))
        }
    }

    fun makeRecorder(): Recorder {
        return Recorder(GraphiteContext_nMakeRecorder(_ptr))
    }

    fun insertRecording(recording: Recording) {
        GraphiteContext_nInsertRecording(_ptr, recording._ptr)
    }

    fun submit(syncCpu: Boolean = false) {
        GraphiteContext_nSubmit(_ptr, syncCpu)
    }
}

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteContext__1nGraphiteMakeMetal")
private external fun GraphiteContext_nMakeMetal(devicePtr: NativePointer, queuePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteContext__1nMakeRecorder")
private external fun GraphiteContext_nMakeRecorder(contextPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteContext__1nInsertRecording")
private external fun GraphiteContext_nInsertRecording(
    contextPtr: NativePointer,
    recordingPtr: NativePointer
)

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteContext__1nSubmit")
private external fun GraphiteContext_nSubmit(contextPtr: NativePointer, syncCpu: Boolean)
