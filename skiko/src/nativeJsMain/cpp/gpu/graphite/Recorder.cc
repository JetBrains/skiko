#include "../../common.h"
#include "include/core/SkImage.h"
#include "include/core/SkTiledImageUtils.h"
#include "src/core/SkLRUCache.h"

#include "include/gpu/graphite/Context.h"
#include "include/gpu/graphite/Recorder.h"
#include "include/gpu/graphite/ImageProvider.h"
#include "include/gpu/graphite/Image.h"

static void deleteRecorder(skgpu::graphite::Recorder* recorder) {
    delete recorder;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_Recorder__1nGetFinalizer
        () {
    return reinterpret_cast<KNativePointer>(&deleteRecorder);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_Recorder__1nSnap(KNativePointer recorderPtr) {
    skgpu::graphite::Recorder *recorder = reinterpret_cast<skgpu::graphite::Recorder*>(recorderPtr);

    std::unique_ptr<skgpu::graphite::Recording> recording = recorder->snap();

    return reinterpret_cast<KNativePointer>(recording.release());
}