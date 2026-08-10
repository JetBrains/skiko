#include <jni.h>
#include <vector>

#include "GraphiteImageProvider.hh"
#include "include/gpu/graphite/BackendSemaphore.h"
#include "include/gpu/graphite/Context.h"
#include "include/gpu/graphite/ContextOptions.h"
#include "include/gpu/graphite/GraphiteTypes.h"
#include "include/gpu/graphite/Recorder.h"
#if defined(SK_METAL)
#include "include/gpu/graphite/mtl/MtlBackendContext.h"
#endif
#if defined(SK_VULKAN)
#include "include/gpu/graphite/vk/VulkanGraphiteContext.h"
#include "include/gpu/graphite/vk/VulkanGraphiteTypes.h"
#include "VulkanUtils.hh"
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
Java_org_jetbrains_skia_gpu_graphite_GraphiteContextKt__1nMakeVulkan(
        JNIEnv*, jclass, jlong instancePtr, jlong physicalDevicePtr, jlong devicePtr,
        jlong queuePtr, jint graphicsQueueIndex, jint maxApiVersion) {
#if defined(SK_VULKAN)
    skgpu::VulkanBackendContext backendContext{};
    backendContext.fInstance = reinterpret_cast<VkInstance>(static_cast<uintptr_t>(instancePtr));
    backendContext.fPhysicalDevice =
            reinterpret_cast<VkPhysicalDevice>(static_cast<uintptr_t>(physicalDevicePtr));
    backendContext.fDevice = reinterpret_cast<VkDevice>(static_cast<uintptr_t>(devicePtr));
    backendContext.fQueue = reinterpret_cast<VkQueue>(static_cast<uintptr_t>(queuePtr));
    backendContext.fGraphicsQueueIndex = graphicsQueueIndex;
    backendContext.fMaxAPIVersion = maxApiVersion;
    backendContext.fGetProc = skikoVulkanGetProc();
    backendContext.fMemoryAllocator =
            skgpu::VulkanMemoryAllocators::Make(backendContext, static_cast<skgpu::ThreadSafe>(true));
    if (!backendContext.fMemoryAllocator) {
        return 0;
    }

    skgpu::graphite::ContextOptions options{};
    options.fRequireOrderedRecordings = true;
    auto context = skgpu::graphite::ContextFactory::MakeVulkan(backendContext, options);
    return reinterpret_cast<jlong>(context.release());
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
        JNIEnv* env,
        jclass,
        jlong contextPtr,
        jlong recordingPtr,
        jlongArray waitSemaphoresPtrs,
        jint waitSemaphoresCount,
        jlongArray signalSemaphoresPtrs,
        jint signalSemaphoresCount) {
    auto context = reinterpret_cast<skgpu::graphite::Context*>(
            static_cast<uintptr_t>(contextPtr));
    if (!context) return;

    skgpu::graphite::InsertRecordingInfo info{};
    info.fRecording = reinterpret_cast<skgpu::graphite::Recording*>(
            static_cast<uintptr_t>(recordingPtr));

    std::vector<skgpu::graphite::BackendSemaphore> waitSemaphores;
    if (waitSemaphoresPtrs && waitSemaphoresCount > 0) {
        waitSemaphores.reserve(waitSemaphoresCount);
        jlong* ptrs = env->GetLongArrayElements(waitSemaphoresPtrs, nullptr);
        for (jint i = 0; i < waitSemaphoresCount; ++i) {
            auto sem = reinterpret_cast<skgpu::graphite::BackendSemaphore*>(
                    static_cast<uintptr_t>(ptrs[i]));
            if (sem) {
                waitSemaphores.push_back(*sem);
            }
        }
        env->ReleaseLongArrayElements(waitSemaphoresPtrs, ptrs, JNI_ABORT);
    }
    info.fNumWaitSemaphores = waitSemaphores.size();
    info.fWaitSemaphores = waitSemaphores.data();

    std::vector<skgpu::graphite::BackendSemaphore> signalSemaphores;
    if (signalSemaphoresPtrs && signalSemaphoresCount > 0) {
        signalSemaphores.reserve(signalSemaphoresCount);
        jlong* ptrs = env->GetLongArrayElements(signalSemaphoresPtrs, nullptr);
        for (jint i = 0; i < signalSemaphoresCount; ++i) {
            auto sem = reinterpret_cast<skgpu::graphite::BackendSemaphore*>(
                    static_cast<uintptr_t>(ptrs[i]));
            if (sem) {
                signalSemaphores.push_back(*sem);
            }
        }
        env->ReleaseLongArrayElements(signalSemaphoresPtrs, ptrs, JNI_ABORT);
    }
    info.fNumSignalSemaphores = signalSemaphores.size();
    info.fSignalSemaphores = signalSemaphores.data();

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
