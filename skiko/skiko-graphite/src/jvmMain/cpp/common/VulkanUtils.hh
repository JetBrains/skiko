#pragma once

#if defined(SK_VULKAN)

#include <memory>

#include <jni.h>

#include "include/core/SkRefCnt.h"
#include "include/gpu/GpuTypes.h"
#include "include/gpu/vk/VulkanBackendContext.h"
#include "include/gpu/vk/VulkanMemoryAllocator.h"
#include "include/third_party/vulkan/vulkan/vulkan_core.h"

#include "VulkanLibrary.hh"

// These declarations mirror Skia private internals used by Skia's own Vulkan examples/tools
// We intentionally call Skia's allocator factory instead of implementing a custom allocator in Skiko
namespace skgpu {
enum class ThreadSafe : bool;
}

namespace skgpu::VulkanMemoryAllocators {
sk_sp<skgpu::VulkanMemoryAllocator> Make(const skgpu::VulkanBackendContext&, skgpu::ThreadSafe);
}

inline skgpu::VulkanGetProc skikoVulkanGetProc() {
    return [](const char* name, VkInstance instance, VkDevice device) {
        return skikoVulkanProc(name, instance, device);
    };
}

inline skgpu::graphite::VulkanTextureInfo skikoVulkanTextureInfoFromIntArray(
        JNIEnv* env,
        jintArray textureInfoValues) {
    jint values[8] = {0};
    if (textureInfoValues) {
        env->GetIntArrayRegion(textureInfoValues, 0, 8, values);
    }

    // Keep these indices in sync with VulkanTextureInfo.packToIntArray() in VulkanTypes.kt.
    return skgpu::graphite::VulkanTextureInfo(
            static_cast<VkSampleCountFlagBits>(values[0]),
            values[1] != 0 ? skgpu::Mipmapped::kYes : skgpu::Mipmapped::kNo,
            static_cast<VkImageCreateFlags>(values[2]),
            static_cast<VkFormat>(values[3]),
            static_cast<VkImageTiling>(values[4]),
            static_cast<VkImageUsageFlags>(values[5]),
            static_cast<VkSharingMode>(values[6]),
            static_cast<VkImageAspectFlags>(values[7]),
            {});
}

#endif
