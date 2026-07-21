#include <jni.h>

#include "GraphiteImageProvider.hh"
#include "include/gpu/graphite/Context.h"
#include "include/gpu/graphite/ContextOptions.h"
#include "include/gpu/graphite/GraphiteTypes.h"
#include "include/gpu/graphite/Recorder.h"
#if defined(SK_METAL)
#include "include/gpu/graphite/mtl/MtlBackendContext.h"
#endif

static void deleteGraphiteContext(skgpu::graphite::Context* context) {
    delete context;
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_gpu_graphite_GraphiteContextKt__1nGetGraphiteContextFinalizer(JNIEnv*, jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteGraphiteContext));
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_gpu_graphite_GraphiteContextKt__1nMakeMetal(
        JNIEnv*, jclass, jlong devicePtr, jlong queuePtr) {
#if defined(SK_METAL)
    skgpu::graphite::MtlBackendContext backendContext{};
    backendContext.fDevice.retain(
            reinterpret_cast<CFTypeRef>(static_cast<uintptr_t>(devicePtr)));
    backendContext.fQueue.retain(
            reinterpret_cast<CFTypeRef>(static_cast<uintptr_t>(queuePtr)));

    skgpu::graphite::ContextOptions options{};
    options.fRequireOrderedRecordings = true;
    return reinterpret_cast<jlong>(
            skgpu::graphite::ContextFactory::MakeMetal(backendContext, options).release());
#else
    return 0;
#endif
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_gpu_graphite_GraphiteContextKt__1nMakeRecorder(
        JNIEnv*, jclass, jlong contextPtr) {
    auto context = reinterpret_cast<skgpu::graphite::Context*>(
            static_cast<uintptr_t>(contextPtr));
    skgpu::graphite::RecorderOptions options{};
    options.fImageProvider = SkikoGraphiteImageProvider::Make();
    return reinterpret_cast<jlong>(context->makeRecorder(options).release());
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_gpu_graphite_GraphiteContextKt__1nInsertRecording(
        JNIEnv*, jclass, jlong contextPtr, jlong recordingPtr) {
    auto context = reinterpret_cast<skgpu::graphite::Context*>(
            static_cast<uintptr_t>(contextPtr));
    skgpu::graphite::InsertRecordingInfo info{};
    info.fRecording = reinterpret_cast<skgpu::graphite::Recording*>(
            static_cast<uintptr_t>(recordingPtr));
    context->insertRecording(info);
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_gpu_graphite_GraphiteContextKt__1nSubmit(
        JNIEnv*, jclass, jlong contextPtr, jboolean syncCpu) {
    auto context = reinterpret_cast<skgpu::graphite::Context*>(
            static_cast<uintptr_t>(contextPtr));
    context->submit(skgpu::graphite::SubmitInfo(
            syncCpu ? skgpu::graphite::SyncToCpu::kYes : skgpu::graphite::SyncToCpu::kNo));
}
