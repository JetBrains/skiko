#include "common.h"

#include "include/gpu/graphite/BackendSemaphore.h"

static void deleteBackendSemaphore(skgpu::graphite::BackendSemaphore* semaphore) {
    delete semaphore;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_BackendSemaphore__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>(&deleteBackendSemaphore);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_gpu_graphite_BackendSemaphore__1nMakeVulkan(
        KNativePointer) {
    return 0;
}
