#include "common.h"
#include "GraphiteImageProvider.hh"

#include "include/gpu/graphite/Context.h"
#include "include/gpu/graphite/ContextOptions.h"
#include "include/gpu/graphite/GraphiteTypes.h"
#include "include/gpu/graphite/Recorder.h"
#include "include/gpu/graphite/mtl/MtlBackendContext.h"

static void deleteGraphiteContext(skgpu::graphite::Context* context) {
    delete context;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_GraphiteContext__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteGraphiteContext);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_GraphiteContext__1nMakeMetal(
        KNativePointer devicePtr, KNativePointer queuePtr) {
    skgpu::graphite::MtlBackendContext backendContext{};
    backendContext.fDevice.retain(reinterpret_cast<CFTypeRef>(devicePtr));
    backendContext.fQueue.retain(reinterpret_cast<CFTypeRef>(queuePtr));

    skgpu::graphite::ContextOptions options{};
    options.fRequireOrderedRecordings = true;
    return reinterpret_cast<KNativePointer>(
            skgpu::graphite::ContextFactory::MakeMetal(backendContext, options).release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_GraphiteContext__1nMakeRecorder(
        KNativePointer contextPtr) {
    auto context = reinterpret_cast<skgpu::graphite::Context*>(contextPtr);
    skgpu::graphite::RecorderOptions options{};
    options.fImageProvider = SkikoGraphiteImageProvider::Make();
    return reinterpret_cast<KNativePointer>(context->makeRecorder(options).release());
}

SKIKO_EXPORT void org_jetbrains_skia_gpu_graphite_GraphiteContext__1nInsertRecording(
        KNativePointer contextPtr, KNativePointer recordingPtr) {
    auto context = reinterpret_cast<skgpu::graphite::Context*>(contextPtr);
    skgpu::graphite::InsertRecordingInfo info{};
    info.fRecording = reinterpret_cast<skgpu::graphite::Recording*>(recordingPtr);
    context->insertRecording(info);
}

SKIKO_EXPORT void org_jetbrains_skia_gpu_graphite_GraphiteContext__1nSubmit(
        KNativePointer contextPtr, KBoolean syncCpu) {
    auto context = reinterpret_cast<skgpu::graphite::Context*>(contextPtr);
    context->submit(skgpu::graphite::SubmitInfo(
            syncCpu ? skgpu::graphite::SyncToCpu::kYes : skgpu::graphite::SyncToCpu::kNo));
}
