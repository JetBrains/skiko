#pragma once

#ifdef SK_VULKAN

#include "include/core/SkRefCnt.h"
#include "include/gpu/vk/VulkanBackendContext.h"
#include "include/gpu/vk/VulkanMemoryAllocator.h"

namespace skgpu {

enum class ThreadSafe : bool {
    kNo = false,
    kYes = true,
};

namespace VulkanMemoryAllocators {
sk_sp<VulkanMemoryAllocator> Make(const VulkanBackendContext&, ThreadSafe);
}  // namespace VulkanMemoryAllocators

}  // namespace skgpu

#endif  // SK_VULKAN
