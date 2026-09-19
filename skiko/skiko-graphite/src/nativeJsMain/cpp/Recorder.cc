#include "common.h"

#include "include/gpu/graphite/Recorder.h"

static void deleteRecorder(skgpu::graphite::Recorder* recorder) {
    delete recorder;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_Recorder__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteRecorder);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_Recorder__1nSnap(
        KNativePointer recorderPtr) {
    auto recorder = reinterpret_cast<skgpu::graphite::Recorder*>(recorderPtr);
    return reinterpret_cast<KNativePointer>(recorder->snap().release());
}
