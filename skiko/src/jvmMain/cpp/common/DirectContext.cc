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
#include "include/gpu/vk/VulkanMemoryAllocator.h"

// simple built-in per-allocation allocator
// this is always expected to be functional
class SimpleVkAllocator : public skgpu::VulkanMemoryAllocator {
public:
    struct Alloc {
        VkDeviceMemory memory;
        VkDeviceSize   size;
        uint32_t       flags; // skgpu::VulkanAlloc flag bits
    };

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

    VkResult allocateImageMemory(VkImage image, uint32_t /*propFlags*/,
                                   skgpu::VulkanBackendMemory* out) override {
        VkMemoryRequirements reqs;
        fGetImgReqs(fDevice, image, &reqs);
        return doAlloc(reqs, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, out);
    }

    VkResult allocateBufferMemory(VkBuffer buffer, BufferUsage usage, uint32_t /*propFlags*/,
                                    skgpu::VulkanBackendMemory* out) override {
        VkMemoryRequirements reqs;
        fGetBufReqs(fDevice, buffer, &reqs);
        return doAlloc(reqs, bufferProps(usage), out);
    }

    void getAllocInfo(const skgpu::VulkanBackendMemory& mem, skgpu::VulkanAlloc* out) const override {
        const Alloc* a = reinterpret_cast<const Alloc*>(mem);
        out->fMemory        = a->memory;
        out->fOffset        = 0;
        out->fSize          = a->size;
        out->fFlags         = a->flags;
        out->fBackendMemory = mem;
    }

    VkResult mapMemory(const skgpu::VulkanBackendMemory& mem, void** data) override {
        return fMapMem(fDevice, reinterpret_cast<const Alloc*>(mem)->memory, 0, VK_WHOLE_SIZE, 0, data);
    }
    void unmapMemory(const skgpu::VulkanBackendMemory& mem) override {
        fUnmapMem(fDevice, reinterpret_cast<const Alloc*>(mem)->memory);
    }
    VkResult flushMemory(const skgpu::VulkanBackendMemory& mem, VkDeviceSize off, VkDeviceSize sz) override {
        VkMappedMemoryRange r = {};
        r.sType = VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE;
        r.memory = reinterpret_cast<const Alloc*>(mem)->memory;
        r.offset = off; r.size = sz;
        return fFlush(fDevice, 1, &r);
    }
    VkResult invalidateMemory(const skgpu::VulkanBackendMemory& mem, VkDeviceSize off, VkDeviceSize sz) override {
        VkMappedMemoryRange r = {};
        r.sType = VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE;
        r.memory = reinterpret_cast<const Alloc*>(mem)->memory;
        r.offset = off; r.size = sz;
        return fInvalidate(fDevice, 1, &r);
    }
    void freeMemory(const skgpu::VulkanBackendMemory& mem) override {
        const Alloc* a = reinterpret_cast<const Alloc*>(mem);
        fFreeMem(fDevice, a->memory, nullptr);
        delete a;
    }
    std::pair<uint64_t, uint64_t> totalAllocatedAndUsedMemory() const override { return {0, 0}; }

private:
    VkPhysicalDevice fPhysDev;
    VkDevice         fDevice;
    PFN_vkGetPhysicalDeviceMemoryProperties  fGetMemProps = nullptr;
    PFN_vkAllocateMemory                     fAllocMem    = nullptr;
    PFN_vkFreeMemory                         fFreeMem     = nullptr;
    PFN_vkMapMemory                          fMapMem      = nullptr;
    PFN_vkUnmapMemory                        fUnmapMem    = nullptr;
    PFN_vkFlushMappedMemoryRanges            fFlush       = nullptr;
    PFN_vkInvalidateMappedMemoryRanges       fInvalidate  = nullptr;
    PFN_vkGetImageMemoryRequirements         fGetImgReqs  = nullptr;
    PFN_vkGetBufferMemoryRequirements        fGetBufReqs  = nullptr;

    uint32_t findMemType(uint32_t bits, VkMemoryPropertyFlags required) const {
        VkPhysicalDeviceMemoryProperties mp = {};
        fGetMemProps(fPhysDev, &mp);
        for (uint32_t i = 0; i < mp.memoryTypeCount; i++) {
            if ((bits & (1u << i)) && (mp.memoryTypes[i].propertyFlags & required) == required)
                return i;
        }
        return ~0u;
    }

    static VkMemoryPropertyFlags bufferProps(BufferUsage u) {
        switch (u) {
            case BufferUsage::kGpuOnly:               return VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
            case BufferUsage::kCpuWritesGpuReads:
            case BufferUsage::kTransfersFromCpuToGpu: return VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
            case BufferUsage::kTransfersFromGpuToCpu: return VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
            default:                                  return VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
        }
    }

    VkResult doAlloc(const VkMemoryRequirements& reqs, VkMemoryPropertyFlags preferred,
                      skgpu::VulkanBackendMemory* out) {
        uint32_t idx = findMemType(reqs.memoryTypeBits, preferred);
        if (idx == ~0u) idx = findMemType(reqs.memoryTypeBits, 0);
        if (idx == ~0u) return VK_ERROR_OUT_OF_DEVICE_MEMORY;

        VkMemoryAllocateInfo ai = {};
        ai.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
        ai.allocationSize  = reqs.size;
        ai.memoryTypeIndex = idx;

        VkDeviceMemory mem;
        VkResult r = fAllocMem(fDevice, &ai, nullptr, &mem);
        if (r != VK_SUCCESS) return r;

        VkPhysicalDeviceMemoryProperties mp = {};
        fGetMemProps(fPhysDev, &mp);
        VkMemoryPropertyFlags props = mp.memoryTypes[idx].propertyFlags;

        uint32_t flags = 0;
        if (!(props & VK_MEMORY_PROPERTY_HOST_COHERENT_BIT))   flags |= skgpu::VulkanAlloc::kNoncoherent_Flag;
        if (  props & VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)     flags |= skgpu::VulkanAlloc::kMappable_Flag;
        if (  props & VK_MEMORY_PROPERTY_LAZILY_ALLOCATED_BIT) flags |= skgpu::VulkanAlloc::kLazilyAllocated_Flag;

        *out = reinterpret_cast<skgpu::VulkanBackendMemory>(new Alloc{mem, reqs.size, flags});
        return VK_SUCCESS;
    }
};

// calls back to the jvm
class JvmVkAllocator : public skgpu::VulkanMemoryAllocator {
public:
    struct AllocRecord {
        VkDeviceMemory memory;
        VkDeviceSize   offset;
        VkDeviceSize   size;
        uint32_t       flags;
    };

    JvmVkAllocator(JNIEnv* env, jobject allocatorObj, VkPhysicalDevice physDev,
                    VkInstance instance, const skgpu::VulkanGetProc& getProc)
        : fPhysDev(physDev) {
        env->GetJavaVM(&fJvm);
        fAllocatorObj = env->NewGlobalRef(allocatorObj);

        fGetMemProps = (PFN_vkGetPhysicalDeviceMemoryProperties)
            getProc("vkGetPhysicalDeviceMemoryProperties", instance, VK_NULL_HANDLE);

        jclass cls = env->GetObjectClass(allocatorObj);

        // allocateImageMemory(Long, Int): Allocation?
        fAllocImageId = env->GetMethodID(cls, "allocateImageMemory",
            "(JI)Lorg/jetbrains/skia/VulkanMemoryAllocator$Allocation;");
        // allocateBufferMemoryBridge(Long, Int, Int): Allocation?
        fAllocBufferId = env->GetMethodID(cls, "allocateBufferMemoryBridge",
            "(JII)Lorg/jetbrains/skia/VulkanMemoryAllocator$Allocation;");
        fMapMemId        = env->GetMethodID(cls, "mapMemory",        "(J)J");
        fUnmapMemId      = env->GetMethodID(cls, "unmapMemory",      "(J)V");
        fFlushMemId      = env->GetMethodID(cls, "flushMemory",      "(JJJ)I");
        fInvalidateMemId = env->GetMethodID(cls, "invalidateMemory", "(JJJ)I");
        fFreeMemId       = env->GetMethodID(cls, "freeMemory",       "(J)V");
        fTotalAllocId    = env->GetMethodID(cls, "totalAllocatedAndUsedMemory", "()Lkotlin/Pair;");
        env->DeleteLocalRef(cls);

        jclass allocCls = env->FindClass("org/jetbrains/skia/VulkanMemoryAllocator$Allocation");
        fGetDeviceMem  = env->GetMethodID(allocCls, "getDeviceMemory",    "()J");
        fGetOffset     = env->GetMethodID(allocCls, "getOffset",          "()J");
        fGetSize       = env->GetMethodID(allocCls, "getSize",            "()J");
        fGetMemTypeIdx = env->GetMethodID(allocCls, "getMemoryTypeIndex", "()I");
        env->DeleteLocalRef(allocCls);

        jclass pairCls = env->FindClass("kotlin/Pair");
        fPairFirst  = env->GetMethodID(pairCls, "getFirst",  "()Ljava/lang/Object;");
        fPairSecond = env->GetMethodID(pairCls, "getSecond", "()Ljava/lang/Object;");
        env->DeleteLocalRef(pairCls);

        jclass longCls = env->FindClass("java/lang/Long");
        fLongValue = env->GetMethodID(longCls, "longValue", "()J");
        env->DeleteLocalRef(longCls);
    }

    ~JvmVkAllocator() override {
        JNIEnv* env = getEnv();
        if (env) env->DeleteGlobalRef(fAllocatorObj);
    }

    VkResult allocateImageMemory(VkImage image, uint32_t propFlags,
                                   skgpu::VulkanBackendMemory* out) override {
        JNIEnv* env = getEnv();
        if (!env) return VK_ERROR_DEVICE_LOST;
        jobject result = env->CallObjectMethod(fAllocatorObj, fAllocImageId,
            (jlong)(uintptr_t)image, (jint)propFlags);
        return unpackAllocation(env, result, out);
    }

    VkResult allocateBufferMemory(VkBuffer buffer, BufferUsage usage, uint32_t propFlags,
                                    skgpu::VulkanBackendMemory* out) override {
        JNIEnv* env = getEnv();
        if (!env) return VK_ERROR_DEVICE_LOST;
        jobject result = env->CallObjectMethod(fAllocatorObj, fAllocBufferId,
            (jlong)(uintptr_t)buffer, (jint)usage, (jint)propFlags);
        return unpackAllocation(env, result, out);
    }

    void getAllocInfo(const skgpu::VulkanBackendMemory& mem, skgpu::VulkanAlloc* out) const override {
        const AllocRecord* r = reinterpret_cast<const AllocRecord*>(mem);
        out->fMemory        = r->memory;
        out->fOffset        = r->offset;
        out->fSize          = r->size;
        out->fFlags         = r->flags;
        out->fBackendMemory = mem;
    }

    VkResult mapMemory(const skgpu::VulkanBackendMemory& mem, void** data) override {
        JNIEnv* env = getEnv();
        if (!env) return VK_ERROR_DEVICE_LOST;
        const AllocRecord* r = reinterpret_cast<const AllocRecord*>(mem);
        jlong ptr = env->CallLongMethod(fAllocatorObj, fMapMemId, (jlong)(uintptr_t)r->memory);
        *data = reinterpret_cast<void*>(static_cast<uintptr_t>(ptr));
        return ptr ? VK_SUCCESS : VK_ERROR_MEMORY_MAP_FAILED;
    }
    void unmapMemory(const skgpu::VulkanBackendMemory& mem) override {
        JNIEnv* env = getEnv();
        if (!env) return;
        const AllocRecord* r = reinterpret_cast<const AllocRecord*>(mem);
        env->CallVoidMethod(fAllocatorObj, fUnmapMemId, (jlong)(uintptr_t)r->memory);
    }
    VkResult flushMemory(const skgpu::VulkanBackendMemory& mem, VkDeviceSize off, VkDeviceSize sz) override {
        JNIEnv* env = getEnv();
        if (!env) return VK_SUCCESS;
        const AllocRecord* r = reinterpret_cast<const AllocRecord*>(mem);
        return (VkResult)env->CallIntMethod(fAllocatorObj, fFlushMemId,
            (jlong)(uintptr_t)r->memory, (jlong)off, (jlong)sz);
    }
    VkResult invalidateMemory(const skgpu::VulkanBackendMemory& mem, VkDeviceSize off, VkDeviceSize sz) override {
        JNIEnv* env = getEnv();
        if (!env) return VK_SUCCESS;
        const AllocRecord* r = reinterpret_cast<const AllocRecord*>(mem);
        return (VkResult)env->CallIntMethod(fAllocatorObj, fInvalidateMemId,
            (jlong)(uintptr_t)r->memory, (jlong)off, (jlong)sz);
    }
    void freeMemory(const skgpu::VulkanBackendMemory& mem) override {
        JNIEnv* env = getEnv();
        AllocRecord* r = reinterpret_cast<AllocRecord*>(mem);
        if (env) env->CallVoidMethod(fAllocatorObj, fFreeMemId, (jlong)(uintptr_t)r->memory);
        delete r;
    }
    std::pair<uint64_t, uint64_t> totalAllocatedAndUsedMemory() const override {
        JNIEnv* env = getEnv();
        if (!env) return {0, 0};
        jobject pair = env->CallObjectMethod(fAllocatorObj, fTotalAllocId);
        if (!pair) return {0, 0};
        jobject first  = env->CallObjectMethod(pair, fPairFirst);
        jobject second = env->CallObjectMethod(pair, fPairSecond);
        jlong total = env->CallLongMethod(first, fLongValue);
        jlong used  = env->CallLongMethod(second, fLongValue);
        env->DeleteLocalRef(first);
        env->DeleteLocalRef(second);
        env->DeleteLocalRef(pair);
        return {(uint64_t)total, (uint64_t)used};
    }

private:
    JavaVM*          fJvm          = nullptr;
    jobject          fAllocatorObj = nullptr; // global ref
    VkPhysicalDevice fPhysDev;
    PFN_vkGetPhysicalDeviceMemoryProperties fGetMemProps = nullptr;

    jmethodID fAllocImageId    = nullptr;
    jmethodID fAllocBufferId   = nullptr;
    jmethodID fMapMemId        = nullptr;
    jmethodID fUnmapMemId      = nullptr;
    jmethodID fFlushMemId      = nullptr;
    jmethodID fInvalidateMemId = nullptr;
    jmethodID fFreeMemId       = nullptr;
    jmethodID fTotalAllocId    = nullptr;
    jmethodID fGetDeviceMem    = nullptr;
    jmethodID fGetOffset       = nullptr;
    jmethodID fGetSize         = nullptr;
    jmethodID fGetMemTypeIdx   = nullptr;
    jmethodID fPairFirst       = nullptr;
    jmethodID fPairSecond      = nullptr;
    jmethodID fLongValue       = nullptr;

    JNIEnv* getEnv() const {
        JNIEnv* env = nullptr;
        if (fJvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) return env;
        if (fJvm->AttachCurrentThread(reinterpret_cast<void**>(&env), nullptr) == JNI_OK) return env;
        return nullptr;
    }

    uint32_t flagsForMemType(uint32_t memTypeIndex) const {
        VkPhysicalDeviceMemoryProperties mp = {};
        fGetMemProps(fPhysDev, &mp);
        if (memTypeIndex >= mp.memoryTypeCount) return 0;
        VkMemoryPropertyFlags props = mp.memoryTypes[memTypeIndex].propertyFlags;
        uint32_t flags = 0;
        if (!(props & VK_MEMORY_PROPERTY_HOST_COHERENT_BIT))   flags |= skgpu::VulkanAlloc::kNoncoherent_Flag;
        if (  props & VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)     flags |= skgpu::VulkanAlloc::kMappable_Flag;
        if (  props & VK_MEMORY_PROPERTY_LAZILY_ALLOCATED_BIT) flags |= skgpu::VulkanAlloc::kLazilyAllocated_Flag;
        return flags;
    }

    VkResult unpackAllocation(JNIEnv* env, jobject resultObj, skgpu::VulkanBackendMemory* out) {
        if (!resultObj) return VK_ERROR_OUT_OF_DEVICE_MEMORY;
        jlong  devMem   = env->CallLongMethod(resultObj, fGetDeviceMem);
        jlong  offset   = env->CallLongMethod(resultObj, fGetOffset);
        jlong  size     = env->CallLongMethod(resultObj, fGetSize);
        jint   memType  = env->CallIntMethod (resultObj, fGetMemTypeIdx);
        env->DeleteLocalRef(resultObj);
        if (!devMem) return VK_ERROR_OUT_OF_DEVICE_MEMORY;
        auto* rec = new AllocRecord{
            reinterpret_cast<VkDeviceMemory>(static_cast<uintptr_t>(devMem)),
            static_cast<VkDeviceSize>(offset),
            static_cast<VkDeviceSize>(size),
            flagsForMemType(static_cast<uint32_t>(memType))
        };
        *out = reinterpret_cast<skgpu::VulkanBackendMemory>(rec);
        return VK_SUCCESS;
    }
};

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_DirectContextKt__1nMakeVulkan
  (JNIEnv* env, jclass jclass,
   jlong instancePtr, jlong physicalDevicePtr, jlong devicePtr, jlong queuePtr,
   jint graphicsQueueIndex, jlong instanceProcAddr, jlong deviceProcAddr, jint apiVersion,
   jobject allocatorObj) {

    skgpu::VulkanBackendContext backendContext = {};
    backendContext.fInstance       = reinterpret_cast<VkInstance>      (static_cast<uintptr_t>(instancePtr));
    backendContext.fPhysicalDevice = reinterpret_cast<VkPhysicalDevice>(static_cast<uintptr_t>(physicalDevicePtr));
    backendContext.fDevice         = reinterpret_cast<VkDevice>        (static_cast<uintptr_t>(devicePtr));
    backendContext.fQueue          = reinterpret_cast<VkQueue>         (static_cast<uintptr_t>(queuePtr));
    backendContext.fGraphicsQueueIndex = static_cast<uint32_t>(graphicsQueueIndex);
    backendContext.fGetProc = [instanceProcAddr, deviceProcAddr](const char* name, VkInstance instance, VkDevice device) -> PFN_vkVoidFunction {
        if (device != VK_NULL_HANDLE)
            return reinterpret_cast<PFN_vkGetDeviceProcAddr>  (static_cast<uintptr_t>(deviceProcAddr))  (device, name);
        return     reinterpret_cast<PFN_vkGetInstanceProcAddr>(static_cast<uintptr_t>(instanceProcAddr))(instance, name);
    };
    backendContext.fMaxAPIVersion = static_cast<uint32_t>(apiVersion);

    skgpu::VulkanExtensions extensions;
    extensions.init(backendContext.fGetProc, backendContext.fInstance, backendContext.fPhysicalDevice,
                    0, nullptr, 0, nullptr);
    backendContext.fVkExtensions = &extensions;

    if (allocatorObj) {
        backendContext.fMemoryAllocator = sk_make_sp<JvmVkAllocator>(
            env, allocatorObj,
            backendContext.fPhysicalDevice,
            backendContext.fInstance,
            backendContext.fGetProc
        );
    } else {
        backendContext.fMemoryAllocator = sk_make_sp<SimpleVkAllocator>(
            backendContext.fPhysicalDevice,
            backendContext.fDevice,
            backendContext.fInstance,
            backendContext.fGetProc
        );
    }

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
