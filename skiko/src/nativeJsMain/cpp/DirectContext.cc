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
#include "include/gpu/vk/VulkanMemoryAllocator.h"

// simple built-in per-allocation allocator
// this is always expected to be functional
class SimpleVkAllocator : public skgpu::VulkanMemoryAllocator {
public:
    struct Alloc { VkDeviceMemory memory; VkDeviceSize size; uint32_t flags; };

    SimpleVkAllocator(VkPhysicalDevice physDev, VkDevice device, VkInstance instance,
                       const skgpu::VulkanGetProc& getProc)
        : fPhysDev(physDev), fDevice(device) {
        fGetMemProps = (PFN_vkGetPhysicalDeviceMemoryProperties)
            getProc("vkGetPhysicalDeviceMemoryProperties", instance, VK_NULL_HANDLE);
        fAllocMem   = (PFN_vkAllocateMemory)               getProc("vkAllocateMemory",               VK_NULL_HANDLE, device);
        fFreeMem    = (PFN_vkFreeMemory)                   getProc("vkFreeMemory",                   VK_NULL_HANDLE, device);
        fMapMem     = (PFN_vkMapMemory)                    getProc("vkMapMemory",                    VK_NULL_HANDLE, device);
        fUnmapMem   = (PFN_vkUnmapMemory)                  getProc("vkUnmapMemory",                  VK_NULL_HANDLE, device);
        fFlush      = (PFN_vkFlushMappedMemoryRanges)      getProc("vkFlushMappedMemoryRanges",      VK_NULL_HANDLE, device);
        fInvalidate = (PFN_vkInvalidateMappedMemoryRanges) getProc("vkInvalidateMappedMemoryRanges", VK_NULL_HANDLE, device);
        fGetImgReqs = (PFN_vkGetImageMemoryRequirements)   getProc("vkGetImageMemoryRequirements",   VK_NULL_HANDLE, device);
        fGetBufReqs = (PFN_vkGetBufferMemoryRequirements)  getProc("vkGetBufferMemoryRequirements",  VK_NULL_HANDLE, device);
    }
    VkResult allocateImageMemory(VkImage image, uint32_t, skgpu::VulkanBackendMemory* out) override {
        VkMemoryRequirements r; fGetImgReqs(fDevice, image, &r);
        return doAlloc(r, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, out);
    }
    VkResult allocateBufferMemory(VkBuffer buf, BufferUsage u, uint32_t, skgpu::VulkanBackendMemory* out) override {
        VkMemoryRequirements r; fGetBufReqs(fDevice, buf, &r);
        VkMemoryPropertyFlags p = (u == BufferUsage::kGpuOnly) ? VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
            : (u == BufferUsage::kTransfersFromGpuToCpu) ? VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
            : (VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        return doAlloc(r, p, out);
    }
    void getAllocInfo(const skgpu::VulkanBackendMemory& m, skgpu::VulkanAlloc* o) const override {
        auto* a = reinterpret_cast<const Alloc*>(m);
        o->fMemory = a->memory; o->fOffset = 0; o->fSize = a->size;
        o->fFlags = a->flags; o->fBackendMemory = m;
    }
    VkResult mapMemory(const skgpu::VulkanBackendMemory& m, void** d) override {
        return fMapMem(fDevice, reinterpret_cast<const Alloc*>(m)->memory, 0, VK_WHOLE_SIZE, 0, d);
    }
    void unmapMemory(const skgpu::VulkanBackendMemory& m) override {
        fUnmapMem(fDevice, reinterpret_cast<const Alloc*>(m)->memory);
    }
    VkResult flushMemory(const skgpu::VulkanBackendMemory& m, VkDeviceSize o, VkDeviceSize s) override {
        VkMappedMemoryRange r = {VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE, nullptr,
                                  reinterpret_cast<const Alloc*>(m)->memory, o, s};
        return fFlush(fDevice, 1, &r);
    }
    VkResult invalidateMemory(const skgpu::VulkanBackendMemory& m, VkDeviceSize o, VkDeviceSize s) override {
        VkMappedMemoryRange r = {VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE, nullptr,
                                  reinterpret_cast<const Alloc*>(m)->memory, o, s};
        return fInvalidate(fDevice, 1, &r);
    }
    void freeMemory(const skgpu::VulkanBackendMemory& m) override {
        auto* a = reinterpret_cast<const Alloc*>(m);
        fFreeMem(fDevice, a->memory, nullptr); delete a;
    }
    std::pair<uint64_t, uint64_t> totalAllocatedAndUsedMemory() const override { return {0,0}; }
private:
    VkPhysicalDevice fPhysDev; VkDevice fDevice;
    PFN_vkGetPhysicalDeviceMemoryProperties  fGetMemProps = nullptr;
    PFN_vkAllocateMemory fAllocMem = nullptr; PFN_vkFreeMemory fFreeMem = nullptr;
    PFN_vkMapMemory fMapMem = nullptr; PFN_vkUnmapMemory fUnmapMem = nullptr;
    PFN_vkFlushMappedMemoryRanges fFlush = nullptr;
    PFN_vkInvalidateMappedMemoryRanges fInvalidate = nullptr;
    PFN_vkGetImageMemoryRequirements fGetImgReqs = nullptr;
    PFN_vkGetBufferMemoryRequirements fGetBufReqs = nullptr;

    uint32_t findMemType(uint32_t bits, VkMemoryPropertyFlags req) const {
        VkPhysicalDeviceMemoryProperties mp = {}; fGetMemProps(fPhysDev, &mp);
        for (uint32_t i = 0; i < mp.memoryTypeCount; i++)
            if ((bits & (1u<<i)) && (mp.memoryTypes[i].propertyFlags & req) == req) return i;
        return ~0u;
    }
    VkResult doAlloc(const VkMemoryRequirements& reqs, VkMemoryPropertyFlags pref,
                      skgpu::VulkanBackendMemory* out) {
        uint32_t idx = findMemType(reqs.memoryTypeBits, pref);
        if (idx == ~0u) idx = findMemType(reqs.memoryTypeBits, 0);
        if (idx == ~0u) return VK_ERROR_OUT_OF_DEVICE_MEMORY;
        VkMemoryAllocateInfo ai = {VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO, nullptr, reqs.size, idx};
        VkDeviceMemory mem; VkResult r = fAllocMem(fDevice, &ai, nullptr, &mem);
        if (r != VK_SUCCESS) return r;
        VkPhysicalDeviceMemoryProperties mp = {}; fGetMemProps(fPhysDev, &mp);
        VkMemoryPropertyFlags props = mp.memoryTypes[idx].propertyFlags;
        uint32_t flags = 0;
        if (!(props & VK_MEMORY_PROPERTY_HOST_COHERENT_BIT))   flags |= skgpu::VulkanAlloc::kNoncoherent_Flag;
        if (  props & VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)     flags |= skgpu::VulkanAlloc::kMappable_Flag;
        if (  props & VK_MEMORY_PROPERTY_LAZILY_ALLOCATED_BIT) flags |= skgpu::VulkanAlloc::kLazilyAllocated_Flag;
        *out = reinterpret_cast<skgpu::VulkanBackendMemory>(new Alloc{mem, reqs.size, flags});
        return VK_SUCCESS;
    }
};
#endif // SK_VULKAN

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
  (KNativePointer instancePtr, KNativePointer physicalDevicePtr, KNativePointer devicePtr,
   KNativePointer queuePtr, KInt graphicsQueueIndex,
   KNativePointer instanceProcAddr, KNativePointer deviceProcAddr, KInt apiVersion,
   KNativePointer /*allocatorPtr*/) {
#ifdef SK_VULKAN
    skgpu::VulkanBackendContext backendContext = {};
    backendContext.fInstance       = reinterpret_cast<VkInstance>      (instancePtr);
    backendContext.fPhysicalDevice = reinterpret_cast<VkPhysicalDevice>(physicalDevicePtr);
    backendContext.fDevice         = reinterpret_cast<VkDevice>        (devicePtr);
    backendContext.fQueue          = reinterpret_cast<VkQueue>         (queuePtr);
    backendContext.fGraphicsQueueIndex = static_cast<uint32_t>(graphicsQueueIndex);
    backendContext.fGetProc = [instanceProcAddr, deviceProcAddr](const char* name, VkInstance instance, VkDevice device) -> PFN_vkVoidFunction {
        if (device != VK_NULL_HANDLE)
            return reinterpret_cast<PFN_vkGetDeviceProcAddr>  (deviceProcAddr)  (device, name);
        return     reinterpret_cast<PFN_vkGetInstanceProcAddr>(instanceProcAddr)(instance, name);
    };
    backendContext.fMaxAPIVersion = static_cast<uint32_t>(apiVersion);

    skgpu::VulkanExtensions extensions;
    extensions.init(backendContext.fGetProc, backendContext.fInstance, backendContext.fPhysicalDevice,
                    0, nullptr, 0, nullptr);
    backendContext.fVkExtensions = &extensions;

    backendContext.fMemoryAllocator = sk_make_sp<SimpleVkAllocator>(
        backendContext.fPhysicalDevice, backendContext.fDevice,
        backendContext.fInstance, backendContext.fGetProc
    );

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

