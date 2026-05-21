#include <iostream>
#include <jni.h>
#include "ganesh/GrDirectContext.h"
#include "ganesh/gl/GrGLAssembleInterface.h"
#include "ganesh/gl/GrGLDirectContext.h"
#include "ganesh/gl/GrGLInterface.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DirectContextKt__1nMakeGL
  (JNIEnv* env, jclass jclass) {
    return reinterpret_cast<jlong>(GrDirectContexts::MakeGL().release());
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DirectContext_1jvmKt__1nMakeGLWithInterface
  (JNIEnv* env, jclass jclass, jlong interfacePtr) {
    sk_sp<const GrGLInterface> interface = sk_ref_sp(reinterpret_cast<const GrGLInterface*>(interfacePtr));
    return reinterpret_cast<jlong>(GrDirectContexts::MakeGL(interface).release());
}

#ifdef SK_METAL
#include "ganesh/mtl/GrMtlBackendContext.h"
#include "ganesh/mtl/GrMtlDirectContext.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DirectContextKt__1nMakeMetal
  (JNIEnv* env, jclass jclass, long devicePtr, long queuePtr) {
    GrMtlBackendContext backendContext = {};
    GrMTLHandle device = reinterpret_cast<GrMTLHandle>(static_cast<uintptr_t>(devicePtr));
    GrMTLHandle queue = reinterpret_cast<GrMTLHandle>(static_cast<uintptr_t>(queuePtr));
    backendContext.fDevice.retain(device);
    backendContext.fQueue.retain(queue);
    sk_sp<GrDirectContext> instance = GrDirectContexts::MakeMetal(backendContext);
    return reinterpret_cast<jlong>(instance.release());
}
#endif

#ifdef SK_DIRECT3D
#include "ganesh/d3d/GrD3DBackendContext.h"
#include "ganesh/d3d/GrD3DDirectContext.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DirectContextKt__1nMakeDirect3D
  (JNIEnv* env, jclass jclass, jlong adapterPtr, jlong devicePtr, jlong queuePtr) {
    GrD3DBackendContext backendContext = {};
    IDXGIAdapter1* adapter = reinterpret_cast<IDXGIAdapter1*>(static_cast<uintptr_t>(adapterPtr));
    ID3D12Device* device = reinterpret_cast<ID3D12Device*>(static_cast<uintptr_t>(devicePtr));
    ID3D12CommandQueue* queue = reinterpret_cast<ID3D12CommandQueue*>(static_cast<uintptr_t>(queuePtr));
    backendContext.fAdapter.retain(adapter);
    backendContext.fDevice.retain(device);
    backendContext.fQueue.retain(queue);
    sk_sp<GrDirectContext> instance = GrDirectContexts::MakeD3D(backendContext);
    return reinterpret_cast<jlong>(instance.release());
}
#endif //SK_DIRECT3D

#ifdef SK_VULKAN
#include "include/gpu/ganesh/vk/GrVkDirectContext.h"
#include "include/gpu/ganesh/vk/GrVkTypes.h"
#include "include/gpu/vk/VulkanBackendContext.h"
#include "include/gpu/vk/VulkanExtensions.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DirectContextKt__1nMakeVulkan
  (JNIEnv* env, jclass jclass, jlong instancePtr, jlong physicalDevicePtr, jlong devicePtr, jlong queuePtr, jint graphicsQueueIndex, jlong instanceProcAddr, jlong deviceProcAddr, jint apiVersion) {
    skgpu::VulkanBackendContext backendContext = {};
    backendContext.fInstance = reinterpret_cast<VkInstance>(static_cast<uintptr_t>(instancePtr));
    backendContext.fPhysicalDevice = reinterpret_cast<VkPhysicalDevice>(static_cast<uintptr_t>(physicalDevicePtr));
    backendContext.fDevice = reinterpret_cast<VkDevice>(static_cast<uintptr_t>(devicePtr));
    backendContext.fQueue = reinterpret_cast<VkQueue>(static_cast<uintptr_t>(queuePtr));
    backendContext.fGraphicsQueueIndex = static_cast<uint32_t>(graphicsQueueIndex);
    backendContext.fGetProc = [instanceProcAddr, deviceProcAddr](const char* name, VkInstance instance, VkDevice device) -> PFN_vkVoidFunction {
        if (device != VK_NULL_HANDLE) {
            return reinterpret_cast<PFN_vkGetDeviceProcAddr>(static_cast<uintptr_t>(deviceProcAddr))(device, name);
        }
        return reinterpret_cast<PFN_vkGetInstanceProcAddr>(static_cast<uintptr_t>(instanceProcAddr))(instance, name);
    };

    skgpu::VulkanExtensions extensions;
    extensions.init(backendContext.fGetProc, backendContext.fInstance, backendContext.fPhysicalDevice, 0, nullptr, 0, nullptr);
    backendContext.fVkExtensions = &extensions;
    backendContext.fMaxAPIVersion = static_cast<uint32_t>(apiVersion);

    sk_sp<GrDirectContext> directContext = GrDirectContexts::MakeVulkan(backendContext);
    return reinterpret_cast<jlong>(directContext.release());
}
#endif // SK_VULKAN

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_DirectContextKt_DirectContext_1nFlushDefault
  (JNIEnv* env, jclass jclass, jlong ptr) {
  GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(ptr));
  context->flush();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_DirectContextKt_DirectContext_1nFlush
  (JNIEnv* env, jclass jclass, jlong ptr, jlong skSurfacePtr) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(ptr));
    SkSurface* skSurface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(skSurfacePtr));
    context->flush(skSurface);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DirectContextKt_DirectContext_1nGetResourceCacheLimit
  (JNIEnv* env, jclass jclass, jlong ptr) {
     GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(ptr));
     return (jlong) context->getResourceCacheLimit();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_DirectContextKt_DirectContext_1nSetResourceCacheLimit
  (JNIEnv* env, jclass jclass, jlong ptr, jlong maxResourceBytes) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(ptr));
    context->setResourceCacheLimit((size_t) maxResourceBytes);
}

GrSyncCpu grSyncCpuFromBool(bool syncCpu) {
  if (syncCpu) return GrSyncCpu::kYes;
  return GrSyncCpu::kNo;
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_DirectContextKt__1nFlushAndSubmit
(JNIEnv* env, jclass jclass, jlong ptr, jlong skSurfacePtr, jboolean syncCpu) {
   GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(ptr));
   SkSurface* skSurface = reinterpret_cast<SkSurface*>(static_cast<uintptr_t>(skSurfacePtr));
   context->flushAndSubmit(skSurface, grSyncCpuFromBool(syncCpu));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_DirectContextKt__1nSubmit
  (JNIEnv* env, jclass jclass, jlong ptr, jboolean syncCpu) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(ptr));
    context->submit(grSyncCpuFromBool(syncCpu));
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_DirectContextKt__1nReset
  (JNIEnv* env, jclass jclass, jlong ptr, jint flags) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(ptr));
    context->resetContext((uint32_t) flags);
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_DirectContextKt__1nAbandon
  (JNIEnv* env, jclass jclass, jlong ptr, jint flags) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(static_cast<uintptr_t>(ptr));
    context->abandonContext();
}
