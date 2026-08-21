#if defined(_WIN32) || defined(__linux__)

#if defined(_WIN32)
#include <windows.h>
#else
#include <dlfcn.h>
#endif

#include <jni.h>

#include "VulkanLibrary.hh"

namespace {

void* vulkanLibrary = nullptr;

bool loadVulkanLibrary() {
#if defined(_WIN32)
    vulkanLibrary = LoadLibrary("vulkan-1.dll");
#else
    vulkanLibrary = dlopen("libvulkan.so.1", RTLD_LAZY | RTLD_LOCAL);
#endif
    return vulkanLibrary != nullptr;
}

}  // namespace

void* skikoVulkanLibrary() {
    return vulkanLibrary;
}

PFN_vkVoidFunction skikoVulkanLibrarySymbol(const char* name) {
#if defined(_WIN32)
    return reinterpret_cast<PFN_vkVoidFunction>(
            GetProcAddress(static_cast<HMODULE>(skikoVulkanLibrary()), name));
#else
    return reinterpret_cast<PFN_vkVoidFunction>(dlsym(skikoVulkanLibrary(), name));
#endif
}

PFN_vkVoidFunction skikoVulkanProc(const char* name, VkInstance instance, VkDevice device) {
    static auto getInstanceProcAddr = reinterpret_cast<PFN_vkGetInstanceProcAddr>(
            skikoVulkanLibrarySymbol("vkGetInstanceProcAddr"));
    if (!getInstanceProcAddr) {
        return nullptr;
    }
    if (device == VK_NULL_HANDLE) {
        return getInstanceProcAddr(instance, name);
    }
    static auto getDeviceProcAddr = reinterpret_cast<PFN_vkGetDeviceProcAddr>(
            skikoVulkanLibrarySymbol("vkGetDeviceProcAddr"));
    return getDeviceProcAddr ? getDeviceProcAddr(device, name) : nullptr;
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_gpu_graphite_GraphiteLibrary_1jvmKt_loadVulkanLibrary(
        JNIEnv* env,
        jobject) {
    if (!loadVulkanLibrary()) {
        env->ThrowNew(env->FindClass("java/lang/UnsatisfiedLinkError"),
                      "Unable to load Vulkan library");
    }
}

#endif
