#include <jni.h>

#include "include/gpu/graphite/BackendSemaphore.h"
#if defined(SK_VULKAN)
#include "include/gpu/graphite/vk/VulkanGraphiteTypes.h"
#endif

static void deleteBackendSemaphore(skgpu::graphite::BackendSemaphore* semaphore) {
    delete semaphore;
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_gpu_graphite_BackendSemaphoreKt__1nGetBackendSemaphoreFinalizer(JNIEnv*, jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBackendSemaphore));
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_gpu_graphite_BackendSemaphoreKt__1nMakeVulkan(
        JNIEnv*, jclass, jlong semaphorePtr) {
#if defined(SK_VULKAN)
    auto semaphore = skgpu::graphite::BackendSemaphores::MakeVulkan(
            reinterpret_cast<VkSemaphore>(static_cast<uintptr_t>(semaphorePtr)));
    return reinterpret_cast<jlong>(new skgpu::graphite::BackendSemaphore(semaphore));
#else
    return 0;
#endif
}
