#include "ganesh/GrDirectContext.h"
#include "ganesh/gl/GrGLInterface.h"
#include "common.h"
#include "ganesh/gl/GrGLDirectContext.h" // TODO: skia update: check if it's correct

#ifdef SK_METAL
#include "ganesh/mtl/GrMtlBackendContext.h"
#include "ganesh/mtl/GrMtlDirectContext.h"
#endif

#ifdef SK_DIRECT3D
#include "ganesh/d3d/GrD3DBackendContext.h"
#include "ganesh/d3d/GrD3DDirectContext.h"
#endif

#ifdef SK_VULKAN
#include "include/gpu/ganesh/vk/GrVkDirectContext.h"
#include "include/gpu/ganesh/vk/GrVkTypes.h"
#include "include/gpu/vk/VulkanBackendContext.h"
#include "include/gpu/vk/VulkanExtensions.h"
#endif

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeGL
  () {
    return static_cast<KNativePointer>(GrDirectContexts::MakeGL().release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeGLWithInterface
  (KNativePointer ptr) {
    sk_sp<GrGLInterface> iface = sk_ref_sp(reinterpret_cast<GrGLInterface*>(ptr));
    return static_cast<KNativePointer>(GrDirectContexts::MakeGL(iface).release());
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeMetal
  (KNativePointer devicePtr, KNativePointer queuePtr) {
#ifdef SK_METAL
    GrMtlBackendContext backendContext = {};
    GrMTLHandle device = reinterpret_cast<GrMTLHandle>((devicePtr));
    GrMTLHandle queue = reinterpret_cast<GrMTLHandle>((queuePtr));
    backendContext.fDevice.retain(device);
    backendContext.fQueue.retain(queue);
    sk_sp<GrDirectContext> instance = GrDirectContexts::MakeMetal(backendContext);
    return static_cast<KNativePointer>(instance.release());
#else
    return nullptr;
#endif // SK_METAL
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeDirect3D
  (KNativePointer adapterPtr, KNativePointer devicePtr, KNativePointer queuePtr) {
#ifdef SK_DIRECT3D
    GrD3DBackendContext backendContext = {};
    IDXGIAdapter1* adapter = reinterpret_cast<IDXGIAdapter1*>(adapterPtr);
    ID3D12Device* device = reinterpret_cast<ID3D12Device*>(devicePtr);
    ID3D12CommandQueue* queue = reinterpret_cast<ID3D12CommandQueue*>(queuePtr);
    backendContext.fAdapter.retain(adapter);
    backendContext.fDevice.retain(device);
    backendContext.fQueue.retain(queue);
    sk_sp<GrDirectContext> instance = GrDirectContexts::MakeD3D(backendContext);
    return static_cast<KNativePointer>(instance.release());
#else // SK_DIRECT3D
    return nullptr;
#endif // SK_DIRECT3D
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_DirectContext__1nMakeVulkan
  (KNativePointer instancePtr, KNativePointer physicalDevicePtr, KNativePointer devicePtr, KNativePointer queuePtr, KInt graphicsQueueIndex, KNativePointer instanceProcAddr, KNativePointer deviceProcAddr, KInt apiVersion) {
#ifdef SK_VULKAN
    skgpu::VulkanBackendContext backendContext = {};
    backendContext.fInstance = reinterpret_cast<VkInstance>(instancePtr);
    backendContext.fPhysicalDevice = reinterpret_cast<VkPhysicalDevice>(physicalDevicePtr);
    backendContext.fDevice = reinterpret_cast<VkDevice>(devicePtr);
    backendContext.fQueue = reinterpret_cast<VkQueue>(queuePtr);
    backendContext.fGraphicsQueueIndex = static_cast<uint32_t>(graphicsQueueIndex);
    backendContext.fGetProc = [instanceProcAddr, deviceProcAddr](const char* name, VkInstance instance, VkDevice device) -> PFN_vkVoidFunction {
        if (device != VK_NULL_HANDLE) {
            return reinterpret_cast<PFN_vkGetDeviceProcAddr>(deviceProcAddr)(device, name);
        }
        return reinterpret_cast<PFN_vkGetInstanceProcAddr>(instanceProcAddr)(instance, name);
    };

    skgpu::VulkanExtensions extensions;
    if (!extensions.init(backendContext.fGetProc, backendContext.fInstance, backendContext.fPhysicalDevice, 0, nullptr, 0, nullptr)) {
        return nullptr;
    }
    backendContext.fVkExtensions = &extensions;
    backendContext.fMaxAPIVersion = static_cast<uint32_t>(apiVersion);

    sk_sp<GrDirectContext> instance = GrDirectContexts::MakeVulkan(backendContext);
    return static_cast<KNativePointer>(instance.release());
#else
    return nullptr;
#endif // SK_VULKAN
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nFlushDefault
  (KNativePointer ptr) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((ptr));
    context->flush(GrFlushInfo());
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nFlush
  (KNativePointer ptr, KNativePointer skSurfacePtr) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(ptr);
    SkSurface* skSurface = reinterpret_cast<SkSurface*>(skSurfacePtr);
    context->flush(skSurface);
}

SKIKO_EXPORT KLong org_jetbrains_skia_DirectContext__1nGetResourceCacheLimit
  (KNativePointer ptr) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(ptr);
    return (KLong) context->getResourceCacheLimit();
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nSetResourceCacheLimit
  (KNativePointer ptr, KLong maxResourceBytes) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(ptr);
    context->setResourceCacheLimit((size_t) maxResourceBytes);
}

GrSyncCpu grSyncCpuFromBool(bool syncCpu) {
    if (syncCpu) return GrSyncCpu::kYes;
    return GrSyncCpu::kNo;
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nFlushAndSubmit
  (KNativePointer ptr, KNativePointer skSurfacePtr, KBoolean syncCpu) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>(ptr);
    SkSurface* skSurface = reinterpret_cast<SkSurface*>(skSurfacePtr);
    context->flushAndSubmit(skSurface, grSyncCpuFromBool(syncCpu));
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nSubmit
  (KNativePointer ptr, KBoolean syncCpu) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((ptr));
    context->submit(grSyncCpuFromBool(syncCpu));
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nReset
  (KNativePointer ptr, KInt flags) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((ptr));
    context->resetContext((uint32_t) flags);
}

SKIKO_EXPORT void org_jetbrains_skia_DirectContext__1nAbandon
  (KNativePointer ptr, KInt flags) {
    GrDirectContext* context = reinterpret_cast<GrDirectContext*>((ptr));
    context->abandonContext();
}

