#include "common.h"

#include "include/gpu/graphite/Recording.h"

static void deleteRecording(skgpu::graphite::Recording* recording) {
    delete recording;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_Recording__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteRecording);
}
