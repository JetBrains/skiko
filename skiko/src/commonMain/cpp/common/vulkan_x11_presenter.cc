#if defined(__linux__)
#define VK_USE_PLATFORM_XLIB_KHR 1
#define VK_NO_PROTOTYPES 1

#include <vulkan/vulkan.h>

#include <X11/Xlib.h>
#include <X11/Xutil.h>

#include <dlfcn.h>

#include <cstdio>
#include <cstdlib>
#include <cstring>

namespace {

struct VulkanFns {
    PFN_vkGetInstanceProcAddr vkGetInstanceProcAddr = nullptr;
    PFN_vkGetDeviceProcAddr vkGetDeviceProcAddr = nullptr;

    PFN_vkCreateInstance vkCreateInstance = nullptr;
    PFN_vkDestroyInstance vkDestroyInstance = nullptr;
    PFN_vkCreateXlibSurfaceKHR vkCreateXlibSurfaceKHR = nullptr;
    PFN_vkDestroySurfaceKHR vkDestroySurfaceKHR = nullptr;
    PFN_vkEnumeratePhysicalDevices vkEnumeratePhysicalDevices = nullptr;
    PFN_vkEnumerateDeviceExtensionProperties vkEnumerateDeviceExtensionProperties = nullptr;
    PFN_vkGetPhysicalDeviceQueueFamilyProperties vkGetPhysicalDeviceQueueFamilyProperties = nullptr;
    PFN_vkGetPhysicalDeviceSurfaceSupportKHR vkGetPhysicalDeviceSurfaceSupportKHR = nullptr;
    PFN_vkGetPhysicalDeviceSurfaceCapabilitiesKHR vkGetPhysicalDeviceSurfaceCapabilitiesKHR = nullptr;
    PFN_vkGetPhysicalDeviceSurfaceFormatsKHR vkGetPhysicalDeviceSurfaceFormatsKHR = nullptr;
    PFN_vkCreateDevice vkCreateDevice = nullptr;

    PFN_vkDestroyDevice vkDestroyDevice = nullptr;
    PFN_vkGetDeviceQueue vkGetDeviceQueue = nullptr;
    PFN_vkDeviceWaitIdle vkDeviceWaitIdle = nullptr;

    PFN_vkCreateSwapchainKHR vkCreateSwapchainKHR = nullptr;
    PFN_vkDestroySwapchainKHR vkDestroySwapchainKHR = nullptr;
    PFN_vkGetSwapchainImagesKHR vkGetSwapchainImagesKHR = nullptr;
    PFN_vkAcquireNextImageKHR vkAcquireNextImageKHR = nullptr;
    PFN_vkQueuePresentKHR vkQueuePresentKHR = nullptr;
    PFN_vkQueueSubmit vkQueueSubmit = nullptr;

    PFN_vkWaitForFences vkWaitForFences = nullptr;
    PFN_vkResetFences vkResetFences = nullptr;
    PFN_vkCreateSemaphore vkCreateSemaphore = nullptr;
    PFN_vkDestroySemaphore vkDestroySemaphore = nullptr;
    PFN_vkCreateFence vkCreateFence = nullptr;
    PFN_vkDestroyFence vkDestroyFence = nullptr;

    PFN_vkCreateCommandPool vkCreateCommandPool = nullptr;
    PFN_vkDestroyCommandPool vkDestroyCommandPool = nullptr;
    PFN_vkAllocateCommandBuffers vkAllocateCommandBuffers = nullptr;
    PFN_vkBeginCommandBuffer vkBeginCommandBuffer = nullptr;
    PFN_vkEndCommandBuffer vkEndCommandBuffer = nullptr;
    PFN_vkResetCommandBuffer vkResetCommandBuffer = nullptr;
    PFN_vkCmdPipelineBarrier vkCmdPipelineBarrier = nullptr;
    PFN_vkCmdCopyBufferToImage vkCmdCopyBufferToImage = nullptr;

    PFN_vkCreateBuffer vkCreateBuffer = nullptr;
    PFN_vkDestroyBuffer vkDestroyBuffer = nullptr;
    PFN_vkGetBufferMemoryRequirements vkGetBufferMemoryRequirements = nullptr;
    PFN_vkGetPhysicalDeviceMemoryProperties vkGetPhysicalDeviceMemoryProperties = nullptr;
    PFN_vkAllocateMemory vkAllocateMemory = nullptr;
    PFN_vkFreeMemory vkFreeMemory = nullptr;
    PFN_vkBindBufferMemory vkBindBufferMemory = nullptr;
    PFN_vkMapMemory vkMapMemory = nullptr;
    PFN_vkUnmapMemory vkUnmapMemory = nullptr;
};

struct Presenter {
    void* vulkanLib = nullptr;
    VulkanFns fn{};

    Display* display = nullptr;
    ::Window window = 0;

    VkInstance instance = VK_NULL_HANDLE;
    VkSurfaceKHR surface = VK_NULL_HANDLE;
    VkPhysicalDevice physicalDevice = VK_NULL_HANDLE;
    VkDevice device = VK_NULL_HANDLE;
    uint32_t queueFamilyIndex = 0;
    VkQueue queue = VK_NULL_HANDLE;

    VkSwapchainKHR swapchain = VK_NULL_HANDLE;
    VkFormat format = VK_FORMAT_UNDEFINED;
    VkColorSpaceKHR colorSpace = VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
    VkExtent2D extent{0, 0};

    VkImage* images = nullptr;
    VkImageLayout* imageLayouts = nullptr;
    uint32_t imageCount = 0;

    VkCommandPool commandPool = VK_NULL_HANDLE;
    VkCommandBuffer* commandBuffers = nullptr;

    VkSemaphore imageAvailable = VK_NULL_HANDLE;
    VkSemaphore renderFinished = VK_NULL_HANDLE;
    VkFence inFlight = VK_NULL_HANDLE;

    VkBuffer stagingBuffer = VK_NULL_HANDLE;
    VkDeviceMemory stagingMemory = VK_NULL_HANDLE;
    void* stagingMapped = nullptr;
    size_t stagingSize = 0;
};

static bool is_vk_ok(VkResult result) { return result == VK_SUCCESS; }
static bool is_vk_ok_or_suboptimal(VkResult result) { return result == VK_SUCCESS || result == VK_SUBOPTIMAL_KHR; }
static bool is_vk_out_of_date(VkResult result) { return result == VK_ERROR_OUT_OF_DATE_KHR; }

template <typename T>
static T dlsym_cast(void* lib, const char* name) {
    return reinterpret_cast<T>(dlsym(lib, name));
}

template <typename T>
static T load_global(Presenter* p, const char* name) {
    return reinterpret_cast<T>(p->fn.vkGetInstanceProcAddr(VK_NULL_HANDLE, name));
}

template <typename T>
static T load_instance(Presenter* p, const char* name) {
    return reinterpret_cast<T>(p->fn.vkGetInstanceProcAddr(p->instance, name));
}

template <typename T>
static T load_device(Presenter* p, const char* name) {
    return reinterpret_cast<T>(p->fn.vkGetDeviceProcAddr(p->device, name));
}

static void unload_vulkan(Presenter* p) {
    if (p->vulkanLib) {
        dlclose(p->vulkanLib);
        p->vulkanLib = nullptr;
    }
    p->fn = VulkanFns{};
}

static void destroy_staging(Presenter* p) {
    if (!p->device) return;
    if (p->stagingMapped && p->stagingMemory) {
        p->fn.vkUnmapMemory(p->device, p->stagingMemory);
        p->stagingMapped = nullptr;
    }
    if (p->stagingBuffer) {
        p->fn.vkDestroyBuffer(p->device, p->stagingBuffer, nullptr);
        p->stagingBuffer = VK_NULL_HANDLE;
    }
    if (p->stagingMemory) {
        p->fn.vkFreeMemory(p->device, p->stagingMemory, nullptr);
        p->stagingMemory = VK_NULL_HANDLE;
    }
    p->stagingSize = 0;
}

static void destroy_swapchain(Presenter* p) {
    if (!p->device) return;

    destroy_staging(p);

    if (p->commandPool) {
        p->fn.vkDestroyCommandPool(p->device, p->commandPool, nullptr);
        p->commandPool = VK_NULL_HANDLE;
    }
    if (p->commandBuffers) {
        free(p->commandBuffers);
        p->commandBuffers = nullptr;
    }

    if (p->swapchain) {
        p->fn.vkDestroySwapchainKHR(p->device, p->swapchain, nullptr);
        p->swapchain = VK_NULL_HANDLE;
    }

    if (p->images) {
        free(p->images);
        p->images = nullptr;
    }
    if (p->imageLayouts) {
        free(p->imageLayouts);
        p->imageLayouts = nullptr;
    }
    p->imageCount = 0;
    p->extent = {0, 0};
    p->format = VK_FORMAT_UNDEFINED;
}

static uint32_t find_memory_type(Presenter* p, uint32_t typeBits, VkMemoryPropertyFlags flags) {
    VkPhysicalDeviceMemoryProperties props{};
    p->fn.vkGetPhysicalDeviceMemoryProperties(p->physicalDevice, &props);

    for (uint32_t i = 0; i < props.memoryTypeCount; ++i) {
        if ((typeBits & (1u << i)) == 0) continue;
        if ((props.memoryTypes[i].propertyFlags & flags) == flags) return i;
    }
    return UINT32_MAX;
}

static bool ensure_staging(Presenter* p, size_t sizeBytes) {
    if (p->stagingMapped && p->stagingSize == sizeBytes) return true;

    destroy_staging(p);

    VkBufferCreateInfo bufferInfo{};
    bufferInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
    bufferInfo.size = static_cast<VkDeviceSize>(sizeBytes);
    bufferInfo.usage = VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
    bufferInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;

    if (!is_vk_ok(p->fn.vkCreateBuffer(p->device, &bufferInfo, nullptr, &p->stagingBuffer))) return false;

    VkMemoryRequirements memReq{};
    p->fn.vkGetBufferMemoryRequirements(p->device, p->stagingBuffer, &memReq);

    const uint32_t memoryTypeIndex = find_memory_type(
        p,
        memReq.memoryTypeBits,
        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
    );
    if (memoryTypeIndex == UINT32_MAX) return false;

    VkMemoryAllocateInfo allocInfo{};
    allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
    allocInfo.allocationSize = memReq.size;
    allocInfo.memoryTypeIndex = memoryTypeIndex;

    if (!is_vk_ok(p->fn.vkAllocateMemory(p->device, &allocInfo, nullptr, &p->stagingMemory))) return false;
    if (!is_vk_ok(p->fn.vkBindBufferMemory(p->device, p->stagingBuffer, p->stagingMemory, 0))) return false;

    if (!is_vk_ok(p->fn.vkMapMemory(p->device, p->stagingMemory, 0, bufferInfo.size, 0, &p->stagingMapped))) return false;

    p->stagingSize = sizeBytes;
    return true;
}

static VkSurfaceFormatKHR choose_surface_format(Presenter* p) {
    uint32_t count = 0;
    p->fn.vkGetPhysicalDeviceSurfaceFormatsKHR(p->physicalDevice, p->surface, &count, nullptr);
    if (count == 0) {
        VkSurfaceFormatKHR fallback{};
        fallback.format = VK_FORMAT_B8G8R8A8_UNORM;
        fallback.colorSpace = VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
        return fallback;
    }

    auto* formats = static_cast<VkSurfaceFormatKHR*>(malloc(sizeof(VkSurfaceFormatKHR) * count));
    p->fn.vkGetPhysicalDeviceSurfaceFormatsKHR(p->physicalDevice, p->surface, &count, formats);

    VkSurfaceFormatKHR chosen = formats[0];
    for (uint32_t i = 0; i < count; ++i) {
        const auto& f = formats[i];
        if (f.format == VK_FORMAT_B8G8R8A8_UNORM && f.colorSpace == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
            chosen = f;
            break;
        }
    }

    free(formats);
    return chosen;
}

static bool create_command_pool_and_buffers(Presenter* p) {
    VkCommandPoolCreateInfo poolInfo{};
    poolInfo.sType = VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
    poolInfo.flags = VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
    poolInfo.queueFamilyIndex = p->queueFamilyIndex;

    if (!is_vk_ok(p->fn.vkCreateCommandPool(p->device, &poolInfo, nullptr, &p->commandPool))) return false;

    p->commandBuffers = static_cast<VkCommandBuffer*>(malloc(sizeof(VkCommandBuffer) * p->imageCount));
    if (!p->commandBuffers) return false;

    VkCommandBufferAllocateInfo allocInfo{};
    allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
    allocInfo.commandPool = p->commandPool;
    allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
    allocInfo.commandBufferCount = p->imageCount;

    return is_vk_ok(p->fn.vkAllocateCommandBuffers(p->device, &allocInfo, p->commandBuffers));
}

static bool record_copy(Presenter* p, uint32_t imageIndex) {
    VkCommandBuffer cmd = p->commandBuffers[imageIndex];

    p->fn.vkResetCommandBuffer(cmd, 0);

    VkCommandBufferBeginInfo begin{};
    begin.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
    begin.flags = VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
    if (!is_vk_ok(p->fn.vkBeginCommandBuffer(cmd, &begin))) return false;

    VkImageMemoryBarrier barrierToTransfer{};
    barrierToTransfer.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
    barrierToTransfer.srcAccessMask = 0;
    barrierToTransfer.dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
    barrierToTransfer.oldLayout = p->imageLayouts[imageIndex];
    barrierToTransfer.newLayout = VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
    barrierToTransfer.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrierToTransfer.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrierToTransfer.image = p->images[imageIndex];
    barrierToTransfer.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    barrierToTransfer.subresourceRange.baseMipLevel = 0;
    barrierToTransfer.subresourceRange.levelCount = 1;
    barrierToTransfer.subresourceRange.baseArrayLayer = 0;
    barrierToTransfer.subresourceRange.layerCount = 1;

    p->fn.vkCmdPipelineBarrier(
        cmd,
        VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
        VK_PIPELINE_STAGE_TRANSFER_BIT,
        0,
        0,
        nullptr,
        0,
        nullptr,
        1,
        &barrierToTransfer
    );

    VkBufferImageCopy region{};
    region.bufferOffset = 0;
    region.bufferRowLength = 0;
    region.bufferImageHeight = 0;
    region.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    region.imageSubresource.mipLevel = 0;
    region.imageSubresource.baseArrayLayer = 0;
    region.imageSubresource.layerCount = 1;
    region.imageOffset = {0, 0, 0};
    region.imageExtent = {p->extent.width, p->extent.height, 1};

    p->fn.vkCmdCopyBufferToImage(
        cmd,
        p->stagingBuffer,
        p->images[imageIndex],
        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
        1,
        &region
    );

    VkImageMemoryBarrier barrierToPresent{};
    barrierToPresent.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
    barrierToPresent.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
    barrierToPresent.dstAccessMask = 0;
    barrierToPresent.oldLayout = VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
    barrierToPresent.newLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
    barrierToPresent.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrierToPresent.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrierToPresent.image = p->images[imageIndex];
    barrierToPresent.subresourceRange = barrierToTransfer.subresourceRange;

    p->fn.vkCmdPipelineBarrier(
        cmd,
        VK_PIPELINE_STAGE_TRANSFER_BIT,
        VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
        0,
        0,
        nullptr,
        0,
        nullptr,
        1,
        &barrierToPresent
    );

    if (!is_vk_ok(p->fn.vkEndCommandBuffer(cmd))) return false;

    p->imageLayouts[imageIndex] = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
    return true;
}

static bool recreate_swapchain(Presenter* p, uint32_t desiredWidth, uint32_t desiredHeight) {
    if (!p->device) return false;

    p->fn.vkDeviceWaitIdle(p->device);
    destroy_swapchain(p);

    VkSurfaceCapabilitiesKHR caps{};
    if (!is_vk_ok(p->fn.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(p->physicalDevice, p->surface, &caps))) return false;

    // Our presenter uploads pixels via vkCmdCopyBufferToImage, so we require TRANSFER_DST support.
    if ((caps.supportedUsageFlags & VK_IMAGE_USAGE_TRANSFER_DST_BIT) == 0) return false;

    const VkSurfaceFormatKHR surfaceFormat = choose_surface_format(p);
    p->format = surfaceFormat.format;
    p->colorSpace = surfaceFormat.colorSpace;

    VkExtent2D extent{};
    if (caps.currentExtent.width != UINT32_MAX) {
        extent = caps.currentExtent;
    } else {
        extent.width = desiredWidth;
        extent.height = desiredHeight;
        if (extent.width < caps.minImageExtent.width) extent.width = caps.minImageExtent.width;
        if (extent.height < caps.minImageExtent.height) extent.height = caps.minImageExtent.height;
        if (extent.width > caps.maxImageExtent.width) extent.width = caps.maxImageExtent.width;
        if (extent.height > caps.maxImageExtent.height) extent.height = caps.maxImageExtent.height;
    }
    p->extent = extent;

    uint32_t imageCount = caps.minImageCount + 1;
    if (caps.maxImageCount != 0 && imageCount > caps.maxImageCount) imageCount = caps.maxImageCount;

    VkCompositeAlphaFlagBitsKHR compositeAlpha = VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
    if ((caps.supportedCompositeAlpha & VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR) != 0) {
        compositeAlpha = VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
    } else if ((caps.supportedCompositeAlpha & VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR) != 0) {
        compositeAlpha = VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR;
    } else if ((caps.supportedCompositeAlpha & VK_COMPOSITE_ALPHA_PRE_MULTIPLIED_BIT_KHR) != 0) {
        compositeAlpha = VK_COMPOSITE_ALPHA_PRE_MULTIPLIED_BIT_KHR;
    } else if ((caps.supportedCompositeAlpha & VK_COMPOSITE_ALPHA_POST_MULTIPLIED_BIT_KHR) != 0) {
        compositeAlpha = VK_COMPOSITE_ALPHA_POST_MULTIPLIED_BIT_KHR;
    }

    VkSwapchainCreateInfoKHR swapInfo{};
    swapInfo.sType = VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
    swapInfo.surface = p->surface;
    swapInfo.minImageCount = imageCount;
    swapInfo.imageFormat = p->format;
    swapInfo.imageColorSpace = p->colorSpace;
    swapInfo.imageExtent = extent;
    swapInfo.imageArrayLayers = 1;
    swapInfo.imageUsage = VK_IMAGE_USAGE_TRANSFER_DST_BIT;
    swapInfo.imageSharingMode = VK_SHARING_MODE_EXCLUSIVE;
    swapInfo.preTransform = caps.currentTransform;
    swapInfo.compositeAlpha = compositeAlpha;
    swapInfo.presentMode = VK_PRESENT_MODE_FIFO_KHR;
    swapInfo.clipped = VK_TRUE;

    if (!is_vk_ok(p->fn.vkCreateSwapchainKHR(p->device, &swapInfo, nullptr, &p->swapchain))) return false;

    p->fn.vkGetSwapchainImagesKHR(p->device, p->swapchain, &p->imageCount, nullptr);
    if (p->imageCount == 0) return false;

    p->images = static_cast<VkImage*>(malloc(sizeof(VkImage) * p->imageCount));
    p->imageLayouts = static_cast<VkImageLayout*>(malloc(sizeof(VkImageLayout) * p->imageCount));
    if (!p->images || !p->imageLayouts) return false;

    p->fn.vkGetSwapchainImagesKHR(p->device, p->swapchain, &p->imageCount, p->images);
    for (uint32_t i = 0; i < p->imageCount; ++i) {
        p->imageLayouts[i] = VK_IMAGE_LAYOUT_UNDEFINED;
    }

    if (!create_command_pool_and_buffers(p)) return false;

    const size_t stagingBytes = static_cast<size_t>(p->extent.width) * static_cast<size_t>(p->extent.height) * 4;
    return ensure_staging(p, stagingBytes);
}

static bool load_global_functions(Presenter* p) {
    p->fn.vkGetInstanceProcAddr = dlsym_cast<PFN_vkGetInstanceProcAddr>(p->vulkanLib, "vkGetInstanceProcAddr");
    if (!p->fn.vkGetInstanceProcAddr) {
        fprintf(stderr, "[SKIKO] vulkan: missing symbol vkGetInstanceProcAddr\n");
        return false;
    }

    // Some loaders don't return vkGetDeviceProcAddr via vkGetInstanceProcAddr(NULL, ...), but it is exported.
    p->fn.vkGetDeviceProcAddr = dlsym_cast<PFN_vkGetDeviceProcAddr>(p->vulkanLib, "vkGetDeviceProcAddr");
    if (!p->fn.vkGetDeviceProcAddr) {
        fprintf(stderr, "[SKIKO] vulkan: missing symbol vkGetDeviceProcAddr\n");
        return false;
    }

    // Global Vulkan entrypoints are expected to be exported by the loader.
    p->fn.vkCreateInstance = dlsym_cast<PFN_vkCreateInstance>(p->vulkanLib, "vkCreateInstance");
    if (!p->fn.vkCreateInstance) {
        p->fn.vkCreateInstance = load_global<PFN_vkCreateInstance>(p, "vkCreateInstance");
    }
    if (!p->fn.vkCreateInstance) {
        fprintf(stderr, "[SKIKO] vulkan: failed to load vkCreateInstance\n");
        return false;
    }
    return true;
}

static bool load_instance_functions(Presenter* p) {
    p->fn.vkDestroyInstance = load_instance<PFN_vkDestroyInstance>(p, "vkDestroyInstance");
    p->fn.vkCreateXlibSurfaceKHR = load_instance<PFN_vkCreateXlibSurfaceKHR>(p, "vkCreateXlibSurfaceKHR");
    p->fn.vkDestroySurfaceKHR = load_instance<PFN_vkDestroySurfaceKHR>(p, "vkDestroySurfaceKHR");
    p->fn.vkEnumeratePhysicalDevices = load_instance<PFN_vkEnumeratePhysicalDevices>(p, "vkEnumeratePhysicalDevices");
    p->fn.vkEnumerateDeviceExtensionProperties =
        load_instance<PFN_vkEnumerateDeviceExtensionProperties>(p, "vkEnumerateDeviceExtensionProperties");
    p->fn.vkGetPhysicalDeviceQueueFamilyProperties =
        load_instance<PFN_vkGetPhysicalDeviceQueueFamilyProperties>(p, "vkGetPhysicalDeviceQueueFamilyProperties");
    p->fn.vkGetPhysicalDeviceSurfaceSupportKHR =
        load_instance<PFN_vkGetPhysicalDeviceSurfaceSupportKHR>(p, "vkGetPhysicalDeviceSurfaceSupportKHR");
    p->fn.vkGetPhysicalDeviceSurfaceCapabilitiesKHR =
        load_instance<PFN_vkGetPhysicalDeviceSurfaceCapabilitiesKHR>(p, "vkGetPhysicalDeviceSurfaceCapabilitiesKHR");
    p->fn.vkGetPhysicalDeviceSurfaceFormatsKHR =
        load_instance<PFN_vkGetPhysicalDeviceSurfaceFormatsKHR>(p, "vkGetPhysicalDeviceSurfaceFormatsKHR");
    p->fn.vkCreateDevice = load_instance<PFN_vkCreateDevice>(p, "vkCreateDevice");

    const bool ok =
        p->fn.vkDestroyInstance && p->fn.vkCreateXlibSurfaceKHR && p->fn.vkDestroySurfaceKHR &&
        p->fn.vkEnumeratePhysicalDevices && p->fn.vkEnumerateDeviceExtensionProperties &&
        p->fn.vkGetPhysicalDeviceQueueFamilyProperties && p->fn.vkGetPhysicalDeviceSurfaceSupportKHR &&
        p->fn.vkGetPhysicalDeviceSurfaceCapabilitiesKHR && p->fn.vkGetPhysicalDeviceSurfaceFormatsKHR &&
        p->fn.vkCreateDevice;
    if (!ok) {
        fprintf(stderr, "[SKIKO] vulkan: failed to load required instance functions\n");
    }
    return ok;
}

static bool load_device_functions(Presenter* p) {
    p->fn.vkDestroyDevice = load_device<PFN_vkDestroyDevice>(p, "vkDestroyDevice");
    p->fn.vkGetDeviceQueue = load_device<PFN_vkGetDeviceQueue>(p, "vkGetDeviceQueue");
    p->fn.vkDeviceWaitIdle = load_device<PFN_vkDeviceWaitIdle>(p, "vkDeviceWaitIdle");

    p->fn.vkCreateSwapchainKHR = load_device<PFN_vkCreateSwapchainKHR>(p, "vkCreateSwapchainKHR");
    p->fn.vkDestroySwapchainKHR = load_device<PFN_vkDestroySwapchainKHR>(p, "vkDestroySwapchainKHR");
    p->fn.vkGetSwapchainImagesKHR = load_device<PFN_vkGetSwapchainImagesKHR>(p, "vkGetSwapchainImagesKHR");
    p->fn.vkAcquireNextImageKHR = load_device<PFN_vkAcquireNextImageKHR>(p, "vkAcquireNextImageKHR");
    p->fn.vkQueuePresentKHR = load_device<PFN_vkQueuePresentKHR>(p, "vkQueuePresentKHR");
    p->fn.vkQueueSubmit = load_device<PFN_vkQueueSubmit>(p, "vkQueueSubmit");

    p->fn.vkWaitForFences = load_device<PFN_vkWaitForFences>(p, "vkWaitForFences");
    p->fn.vkResetFences = load_device<PFN_vkResetFences>(p, "vkResetFences");
    p->fn.vkCreateSemaphore = load_device<PFN_vkCreateSemaphore>(p, "vkCreateSemaphore");
    p->fn.vkDestroySemaphore = load_device<PFN_vkDestroySemaphore>(p, "vkDestroySemaphore");
    p->fn.vkCreateFence = load_device<PFN_vkCreateFence>(p, "vkCreateFence");
    p->fn.vkDestroyFence = load_device<PFN_vkDestroyFence>(p, "vkDestroyFence");

    p->fn.vkCreateCommandPool = load_device<PFN_vkCreateCommandPool>(p, "vkCreateCommandPool");
    p->fn.vkDestroyCommandPool = load_device<PFN_vkDestroyCommandPool>(p, "vkDestroyCommandPool");
    p->fn.vkAllocateCommandBuffers = load_device<PFN_vkAllocateCommandBuffers>(p, "vkAllocateCommandBuffers");
    p->fn.vkBeginCommandBuffer = load_device<PFN_vkBeginCommandBuffer>(p, "vkBeginCommandBuffer");
    p->fn.vkEndCommandBuffer = load_device<PFN_vkEndCommandBuffer>(p, "vkEndCommandBuffer");
    p->fn.vkResetCommandBuffer = load_device<PFN_vkResetCommandBuffer>(p, "vkResetCommandBuffer");
    p->fn.vkCmdPipelineBarrier = load_device<PFN_vkCmdPipelineBarrier>(p, "vkCmdPipelineBarrier");
    p->fn.vkCmdCopyBufferToImage = load_device<PFN_vkCmdCopyBufferToImage>(p, "vkCmdCopyBufferToImage");

    p->fn.vkCreateBuffer = load_device<PFN_vkCreateBuffer>(p, "vkCreateBuffer");
    p->fn.vkDestroyBuffer = load_device<PFN_vkDestroyBuffer>(p, "vkDestroyBuffer");
    p->fn.vkGetBufferMemoryRequirements = load_device<PFN_vkGetBufferMemoryRequirements>(p, "vkGetBufferMemoryRequirements");
    p->fn.vkAllocateMemory = load_device<PFN_vkAllocateMemory>(p, "vkAllocateMemory");
    p->fn.vkFreeMemory = load_device<PFN_vkFreeMemory>(p, "vkFreeMemory");
    p->fn.vkBindBufferMemory = load_device<PFN_vkBindBufferMemory>(p, "vkBindBufferMemory");
    p->fn.vkMapMemory = load_device<PFN_vkMapMemory>(p, "vkMapMemory");
    p->fn.vkUnmapMemory = load_device<PFN_vkUnmapMemory>(p, "vkUnmapMemory");

    // This one is a physical-device function, but exposed as global in Vulkan 1.0; load via instance.
    p->fn.vkGetPhysicalDeviceMemoryProperties =
        load_instance<PFN_vkGetPhysicalDeviceMemoryProperties>(p, "vkGetPhysicalDeviceMemoryProperties");

    const bool ok =
        p->fn.vkDestroyDevice && p->fn.vkGetDeviceQueue && p->fn.vkDeviceWaitIdle &&
        p->fn.vkCreateSwapchainKHR && p->fn.vkDestroySwapchainKHR && p->fn.vkGetSwapchainImagesKHR &&
        p->fn.vkAcquireNextImageKHR && p->fn.vkQueuePresentKHR && p->fn.vkQueueSubmit &&
        p->fn.vkWaitForFences && p->fn.vkResetFences && p->fn.vkCreateSemaphore && p->fn.vkDestroySemaphore &&
        p->fn.vkCreateFence && p->fn.vkDestroyFence && p->fn.vkCreateCommandPool && p->fn.vkDestroyCommandPool &&
        p->fn.vkAllocateCommandBuffers && p->fn.vkBeginCommandBuffer && p->fn.vkEndCommandBuffer &&
        p->fn.vkResetCommandBuffer && p->fn.vkCmdPipelineBarrier && p->fn.vkCmdCopyBufferToImage &&
        p->fn.vkCreateBuffer && p->fn.vkDestroyBuffer && p->fn.vkGetBufferMemoryRequirements &&
        p->fn.vkAllocateMemory && p->fn.vkFreeMemory && p->fn.vkBindBufferMemory && p->fn.vkMapMemory &&
        p->fn.vkUnmapMemory && p->fn.vkGetPhysicalDeviceMemoryProperties;
    if (!ok) {
        fprintf(stderr, "[SKIKO] vulkan: failed to load required device functions\n");
    }
    return ok;
}

static bool init_instance_and_device(Presenter* p) {
    p->vulkanLib = dlopen("libvulkan.so.1", RTLD_NOW | RTLD_LOCAL);
    if (!p->vulkanLib) {
        fprintf(stderr, "[SKIKO] vulkan: dlopen(libvulkan.so.1) failed: %s\n", dlerror());
        return false;
    }

    if (!load_global_functions(p)) {
        fprintf(stderr, "[SKIKO] vulkan: failed to load global Vulkan functions\n");
        return false;
    }

    const char* instanceExts[] = {VK_KHR_SURFACE_EXTENSION_NAME, VK_KHR_XLIB_SURFACE_EXTENSION_NAME};

    VkApplicationInfo app{};
    app.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    app.pApplicationName = "Skiko";
    app.applicationVersion = VK_MAKE_VERSION(0, 1, 0);
    app.pEngineName = "Skiko";
    app.engineVersion = VK_MAKE_VERSION(0, 1, 0);
    app.apiVersion = VK_API_VERSION_1_0;

    VkInstanceCreateInfo create{};
    create.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    create.pApplicationInfo = &app;
    create.enabledExtensionCount = 2;
    create.ppEnabledExtensionNames = instanceExts;

    VkResult instanceRes = p->fn.vkCreateInstance(&create, nullptr, &p->instance);
    if (!is_vk_ok(instanceRes)) {
        fprintf(stderr, "[SKIKO] vulkan: vkCreateInstance failed: %d\n", static_cast<int>(instanceRes));
        return false;
    }
    if (!load_instance_functions(p)) return false;

    VkXlibSurfaceCreateInfoKHR surfInfo{};
    surfInfo.sType = VK_STRUCTURE_TYPE_XLIB_SURFACE_CREATE_INFO_KHR;
    surfInfo.dpy = p->display;
    surfInfo.window = p->window;

    VkResult surfaceRes = p->fn.vkCreateXlibSurfaceKHR(p->instance, &surfInfo, nullptr, &p->surface);
    if (!is_vk_ok(surfaceRes)) {
        fprintf(stderr, "[SKIKO] vulkan: vkCreateXlibSurfaceKHR failed: %d\n", static_cast<int>(surfaceRes));
        return false;
    }

    uint32_t deviceCount = 0;
    p->fn.vkEnumeratePhysicalDevices(p->instance, &deviceCount, nullptr);
    if (deviceCount == 0) {
        fprintf(stderr, "[SKIKO] vulkan: no physical devices found\n");
        return false;
    }

    auto* physicalDevices = static_cast<VkPhysicalDevice*>(malloc(sizeof(VkPhysicalDevice) * deviceCount));
    p->fn.vkEnumeratePhysicalDevices(p->instance, &deviceCount, physicalDevices);

    const char* deviceExts[] = {VK_KHR_SWAPCHAIN_EXTENSION_NAME};

    for (uint32_t d = 0; d < deviceCount; ++d) {
        VkPhysicalDevice pd = physicalDevices[d];

        // Check swapchain extension.
        uint32_t extCount = 0;
        p->fn.vkEnumerateDeviceExtensionProperties(pd, nullptr, &extCount, nullptr);
        bool hasSwapchain = false;
        if (extCount > 0) {
            auto* exts = static_cast<VkExtensionProperties*>(malloc(sizeof(VkExtensionProperties) * extCount));
            p->fn.vkEnumerateDeviceExtensionProperties(pd, nullptr, &extCount, exts);
            for (uint32_t i = 0; i < extCount; ++i) {
                if (strcmp(exts[i].extensionName, VK_KHR_SWAPCHAIN_EXTENSION_NAME) == 0) {
                    hasSwapchain = true;
                    break;
                }
            }
            free(exts);
        }
        if (!hasSwapchain) continue;

        uint32_t queueFamilyCount = 0;
        p->fn.vkGetPhysicalDeviceQueueFamilyProperties(pd, &queueFamilyCount, nullptr);
        if (queueFamilyCount == 0) continue;

        auto* queueFamilies = static_cast<VkQueueFamilyProperties*>(malloc(sizeof(VkQueueFamilyProperties) * queueFamilyCount));
        p->fn.vkGetPhysicalDeviceQueueFamilyProperties(pd, &queueFamilyCount, queueFamilies);

        for (uint32_t q = 0; q < queueFamilyCount; ++q) {
            const bool supportsGraphics = (queueFamilies[q].queueFlags & VK_QUEUE_GRAPHICS_BIT) != 0;
            if (!supportsGraphics) continue;

            VkBool32 presentSupported = VK_FALSE;
            p->fn.vkGetPhysicalDeviceSurfaceSupportKHR(pd, q, p->surface, &presentSupported);
            if (!presentSupported) continue;

            p->physicalDevice = pd;
            p->queueFamilyIndex = q;
            free(queueFamilies);

            const float priority = 1.0f;
            VkDeviceQueueCreateInfo queueInfo{};
            queueInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
            queueInfo.queueFamilyIndex = q;
            queueInfo.queueCount = 1;
            queueInfo.pQueuePriorities = &priority;

            VkDeviceCreateInfo deviceInfo{};
            deviceInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
            deviceInfo.queueCreateInfoCount = 1;
            deviceInfo.pQueueCreateInfos = &queueInfo;
            deviceInfo.enabledExtensionCount = 1;
            deviceInfo.ppEnabledExtensionNames = deviceExts;

            if (!is_vk_ok(p->fn.vkCreateDevice(pd, &deviceInfo, nullptr, &p->device))) {
                fprintf(stderr, "[SKIKO] vulkan: vkCreateDevice failed\n");
                free(physicalDevices);
                return false;
            }

            // Device proc table.
            if (!load_device_functions(p)) {
                fprintf(stderr, "[SKIKO] vulkan: failed to load device Vulkan functions\n");
                free(physicalDevices);
                return false;
            }

            p->fn.vkGetDeviceQueue(p->device, q, 0, &p->queue);

            free(physicalDevices);
            return true;
        }
        free(queueFamilies);
    }

    free(physicalDevices);
    fprintf(stderr, "[SKIKO] vulkan: no suitable device/queue family found\n");
    return false;
}

static bool create_sync_objects(Presenter* p) {
    VkSemaphoreCreateInfo sem{};
    sem.sType = VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
    VkResult imageAvailableRes = p->fn.vkCreateSemaphore(p->device, &sem, nullptr, &p->imageAvailable);
    if (!is_vk_ok(imageAvailableRes)) {
        fprintf(stderr, "[SKIKO] vulkan: vkCreateSemaphore(imageAvailable) failed: %d\n", static_cast<int>(imageAvailableRes));
        return false;
    }
    VkResult renderFinishedRes = p->fn.vkCreateSemaphore(p->device, &sem, nullptr, &p->renderFinished);
    if (!is_vk_ok(renderFinishedRes)) {
        fprintf(stderr, "[SKIKO] vulkan: vkCreateSemaphore(renderFinished) failed: %d\n", static_cast<int>(renderFinishedRes));
        return false;
    }

    VkFenceCreateInfo fence{};
    fence.sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
    fence.flags = VK_FENCE_CREATE_SIGNALED_BIT;
    VkResult fenceRes = p->fn.vkCreateFence(p->device, &fence, nullptr, &p->inFlight);
    if (!is_vk_ok(fenceRes)) {
        fprintf(stderr, "[SKIKO] vulkan: vkCreateFence(inFlight) failed: %d\n", static_cast<int>(fenceRes));
        return false;
    }
    return true;
}

} // namespace

extern "C" {

void skiko_vulkan_x11_destroy(void* handle);

void* skiko_vulkan_x11_create(void* displayPtr, unsigned long window) {
    auto* p = new Presenter();
    p->display = reinterpret_cast<Display*>(displayPtr);
    p->window = static_cast<::Window>(window);

    if (!init_instance_and_device(p)) {
        skiko_vulkan_x11_destroy(p);
        return nullptr;
    }
    if (!create_sync_objects(p)) {
        skiko_vulkan_x11_destroy(p);
        return nullptr;
    }

    // Create swapchain lazily (first present call).
    return p;
}

void skiko_vulkan_x11_destroy(void* handle) {
    auto* p = reinterpret_cast<Presenter*>(handle);
    if (!p) return;

    if (p->device) {
        p->fn.vkDeviceWaitIdle(p->device);
    }

    destroy_swapchain(p);

    if (p->device) {
        if (p->imageAvailable) p->fn.vkDestroySemaphore(p->device, p->imageAvailable, nullptr);
        if (p->renderFinished) p->fn.vkDestroySemaphore(p->device, p->renderFinished, nullptr);
        if (p->inFlight) p->fn.vkDestroyFence(p->device, p->inFlight, nullptr);
        p->fn.vkDestroyDevice(p->device, nullptr);
        p->device = VK_NULL_HANDLE;
    }

    if (p->instance) {
        if (p->surface) p->fn.vkDestroySurfaceKHR(p->instance, p->surface, nullptr);
        p->fn.vkDestroyInstance(p->instance, nullptr);
        p->instance = VK_NULL_HANDLE;
    }

    unload_vulkan(p);
    delete p;
}

int skiko_vulkan_x11_get_pixel_format(void* handle) {
    auto* p = reinterpret_cast<Presenter*>(handle);
    if (!p) return 0;
    // 0 = BGRA, 1 = RGBA
    switch (p->format) {
        case VK_FORMAT_R8G8B8A8_UNORM:
        case VK_FORMAT_R8G8B8A8_SRGB:
            return 1;
        default:
            return 0;
    }
}

int skiko_vulkan_x11_present(void* handle, const void* pixels, int width, int height, int rowBytes) {
    auto* p = reinterpret_cast<Presenter*>(handle);
    if (!p || !p->device || !p->queue) return VK_ERROR_INITIALIZATION_FAILED;

    const uint32_t desiredWidth = width > 0 ? static_cast<uint32_t>(width) : 1u;
    const uint32_t desiredHeight = height > 0 ? static_cast<uint32_t>(height) : 1u;

    if (!p->swapchain || p->extent.width != desiredWidth || p->extent.height != desiredHeight) {
        if (!recreate_swapchain(p, desiredWidth, desiredHeight)) return VK_ERROR_INITIALIZATION_FAILED;
    }

    const size_t expectedBytes = static_cast<size_t>(p->extent.width) * static_cast<size_t>(p->extent.height) * 4;
    if (!ensure_staging(p, expectedBytes)) return VK_ERROR_INITIALIZATION_FAILED;

    const auto* src = static_cast<const unsigned char*>(pixels);
    auto* dst = static_cast<unsigned char*>(p->stagingMapped);
    const size_t dstRowBytes = static_cast<size_t>(p->extent.width) * 4;
    const size_t srcRowBytes = rowBytes > 0 ? static_cast<size_t>(rowBytes) : static_cast<size_t>(desiredWidth) * 4;

    // The swapchain extent may be clamped by the surface capabilities. Copy as much as we can
    // from the provided buffer and pad/crop as needed to avoid out-of-bounds reads.
    if (desiredWidth == p->extent.width && desiredHeight == p->extent.height && srcRowBytes == dstRowBytes) {
        memcpy(dst, src, expectedBytes);
    } else {
        const uint32_t srcRows = desiredHeight;
        for (uint32_t y = 0; y < p->extent.height; ++y) {
            const size_t dstOff = static_cast<size_t>(y) * dstRowBytes;
            if (y >= srcRows) {
                memset(dst + dstOff, 0, dstRowBytes);
                continue;
            }

            const size_t srcOff = static_cast<size_t>(y) * srcRowBytes;
            const size_t copyBytes = srcRowBytes < dstRowBytes ? srcRowBytes : dstRowBytes;
            memcpy(dst + dstOff, src + srcOff, copyBytes);
            if (copyBytes < dstRowBytes) {
                memset(dst + dstOff + copyBytes, 0, dstRowBytes - copyBytes);
            }
        }
    }

    p->fn.vkWaitForFences(p->device, 1, &p->inFlight, VK_TRUE, UINT64_MAX);
    p->fn.vkResetFences(p->device, 1, &p->inFlight);

    uint32_t imageIndex = 0;
    VkResult acquire = p->fn.vkAcquireNextImageKHR(
        p->device,
        p->swapchain,
        UINT64_MAX,
        p->imageAvailable,
        VK_NULL_HANDLE,
        &imageIndex
    );
    if (is_vk_out_of_date(acquire) || acquire == VK_SUBOPTIMAL_KHR) {
        if (!recreate_swapchain(p, desiredWidth, desiredHeight)) return VK_ERROR_INITIALIZATION_FAILED;
        acquire = p->fn.vkAcquireNextImageKHR(
            p->device,
            p->swapchain,
            UINT64_MAX,
            p->imageAvailable,
            VK_NULL_HANDLE,
            &imageIndex
        );
    }
    if (!is_vk_ok_or_suboptimal(acquire)) return acquire;

    if (!record_copy(p, imageIndex)) return VK_ERROR_INITIALIZATION_FAILED;

    VkPipelineStageFlags waitStages[] = {VK_PIPELINE_STAGE_TRANSFER_BIT};
    VkSubmitInfo submit{};
    submit.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
    submit.waitSemaphoreCount = 1;
    submit.pWaitSemaphores = &p->imageAvailable;
    submit.pWaitDstStageMask = waitStages;
    submit.commandBufferCount = 1;
    submit.pCommandBuffers = &p->commandBuffers[imageIndex];
    submit.signalSemaphoreCount = 1;
    submit.pSignalSemaphores = &p->renderFinished;

    VkResult submitRes = p->fn.vkQueueSubmit(p->queue, 1, &submit, p->inFlight);
    if (!is_vk_ok(submitRes)) return submitRes;

    VkPresentInfoKHR present{};
    present.sType = VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
    present.waitSemaphoreCount = 1;
    present.pWaitSemaphores = &p->renderFinished;
    present.swapchainCount = 1;
    present.pSwapchains = &p->swapchain;
    present.pImageIndices = &imageIndex;

    VkResult presentRes = p->fn.vkQueuePresentKHR(p->queue, &present);
    if (is_vk_out_of_date(presentRes) || presentRes == VK_SUBOPTIMAL_KHR) {
        // Recreate swapchain for subsequent frames; this frame may be dropped.
        if (!recreate_swapchain(p, desiredWidth, desiredHeight)) return VK_ERROR_INITIALIZATION_FAILED;
    }
    return presentRes;
}

} // extern "C"

#else

extern "C" {
void* skiko_vulkan_x11_create(void*, unsigned long) { return nullptr; }
void skiko_vulkan_x11_destroy(void*) {}
int skiko_vulkan_x11_get_pixel_format(void*) { return 0; }
int skiko_vulkan_x11_present(void*, const void*, int, int, int) { return -1; }
}

#endif
