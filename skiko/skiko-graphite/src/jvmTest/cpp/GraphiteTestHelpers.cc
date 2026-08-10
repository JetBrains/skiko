#include <jni.h>

#if defined(SK_VULKAN)
#include "include/third_party/vulkan/vulkan/vulkan_core.h"
#include <vector>

extern "C" JNIEXPORT jlongArray JNICALL
Java_org_jetbrains_skia_gpu_graphite_GraphiteTestHelpersKt__1nCreateVulkanObjects(
        JNIEnv* env,
        jclass) {
    VkApplicationInfo appInfo{};
    appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.pApplicationName = "SkikoGraphiteTest";
    // Skia requires Vulkan 1.1 as the minimum version.
    appInfo.apiVersion = VK_API_VERSION_1_1;

    VkInstanceCreateInfo instanceCreateInfo{};
    instanceCreateInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    instanceCreateInfo.pApplicationInfo = &appInfo;

    VkInstance instance = VK_NULL_HANDLE;
    if (vkCreateInstance(&instanceCreateInfo, nullptr, &instance) != VK_SUCCESS) {
        return env->NewLongArray(0);
    }

    uint32_t physicalDevicesCount = 0;
    if (vkEnumeratePhysicalDevices(instance, &physicalDevicesCount, nullptr) != VK_SUCCESS ||
        physicalDevicesCount == 0) {
        vkDestroyInstance(instance, nullptr);
        return env->NewLongArray(0);
    }

    std::vector<VkPhysicalDevice> physicalDevices(physicalDevicesCount);
    if (vkEnumeratePhysicalDevices(instance, &physicalDevicesCount, physicalDevices.data()) != VK_SUCCESS) {
        vkDestroyInstance(instance, nullptr);
        return env->NewLongArray(0);
    }

    VkPhysicalDevice physicalDevice = physicalDevices[0];
    uint32_t queueFamilyCount = 0;
    vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, &queueFamilyCount, nullptr);
    if (queueFamilyCount == 0) {
        vkDestroyInstance(instance, nullptr);
        return env->NewLongArray(0);
    }

    std::vector<VkQueueFamilyProperties> queueFamilies(queueFamilyCount);
    vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, &queueFamilyCount, queueFamilies.data());

    uint32_t graphicsQueueIndex = 0;
    bool hasGraphicsQueue = false;
    for (uint32_t q = 0; q < queueFamilyCount; ++q) {
        if (queueFamilies[q].queueCount > 0 &&
            (queueFamilies[q].queueFlags & VK_QUEUE_GRAPHICS_BIT) != 0) {
            graphicsQueueIndex = q;
            hasGraphicsQueue = true;
            break;
        }
    }

    if (!hasGraphicsQueue) {
        vkDestroyInstance(instance, nullptr);
        return env->NewLongArray(0);
    }

    float queuePriority = 1.0f;
    VkDeviceQueueCreateInfo queueCreateInfo{};
    queueCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
    queueCreateInfo.queueFamilyIndex = graphicsQueueIndex;
    queueCreateInfo.queueCount = 1;
    queueCreateInfo.pQueuePriorities = &queuePriority;

    VkDeviceCreateInfo deviceCreateInfo{};
    deviceCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
    deviceCreateInfo.queueCreateInfoCount = 1;
    deviceCreateInfo.pQueueCreateInfos = &queueCreateInfo;

    VkDevice device = VK_NULL_HANDLE;
    if (vkCreateDevice(physicalDevice, &deviceCreateInfo, nullptr, &device) != VK_SUCCESS) {
        vkDestroyInstance(instance, nullptr);
        return env->NewLongArray(0);
    }

    VkQueue queue = VK_NULL_HANDLE;
    vkGetDeviceQueue(device, graphicsQueueIndex, 0, &queue);
    if (queue == VK_NULL_HANDLE) {
        vkDestroyDevice(device, nullptr);
        vkDestroyInstance(instance, nullptr);
        return env->NewLongArray(0);
    }
    // Skia requires Vulkan 1.1 as the minimum version.
    uint32_t apiVersion = VK_API_VERSION_1_1;

    jlong pointers[] = {
            reinterpret_cast<jlong>(instance),
            reinterpret_cast<jlong>(physicalDevice),
            reinterpret_cast<jlong>(device),
            reinterpret_cast<jlong>(queue),
            static_cast<jlong>(graphicsQueueIndex),
            static_cast<jlong>(apiVersion),
    };
    jlongArray result = env->NewLongArray(6);
    env->SetLongArrayRegion(result, 0, 6, pointers);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_gpu_graphite_GraphiteTestHelpersKt__1nReleaseVulkanObjects(
        JNIEnv*,
        jclass,
        jlong devicePtr,
        jlong instancePtr) {
    auto device = reinterpret_cast<VkDevice>(static_cast<uintptr_t>(devicePtr));
    auto instance = reinterpret_cast<VkInstance>(static_cast<uintptr_t>(instancePtr));
    if (device != VK_NULL_HANDLE) {
        vkDeviceWaitIdle(device);
        vkDestroyDevice(device, nullptr);
    }
    if (instance != VK_NULL_HANDLE) {
        vkDestroyInstance(instance, nullptr);
    }
}

#else

extern "C" JNIEXPORT jlongArray JNICALL
Java_org_jetbrains_skia_gpu_graphite_GraphiteTestHelpersKt__1nCreateVulkanObjects(
        JNIEnv* env,
        jclass) {
    return env->NewLongArray(0);
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_gpu_graphite_GraphiteTestHelpersKt__1nReleaseVulkanObjects(
        JNIEnv*,
        jclass,
        jlong,
        jlong) {}

#endif
