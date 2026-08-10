#include <jni.h>

#include "include/gpu/graphite/BackendTexture.h"
#if defined(SK_METAL)
#include "include/gpu/graphite/mtl/MtlGraphiteTypes_cpp.h"
#endif
#if defined(SK_VULKAN)
#include "include/gpu/graphite/vk/VulkanGraphiteTypes.h"
#include "VulkanUtils.hh"
#endif

static void deleteBackendTexture(skgpu::graphite::BackendTexture* texture) {
    delete texture;
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_gpu_graphite_BackendTextureKt__1nGetBackendTextureFinalizer(JNIEnv*, jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteBackendTexture));
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_gpu_graphite_BackendTextureKt__1nMakeMetal(
        JNIEnv*, jclass, jint width, jint height, jlong texturePtr) {
#if defined(SK_METAL)
    auto texture = skgpu::graphite::BackendTextures::MakeMetal(
            SkISize::Make(width, height),
            reinterpret_cast<CFTypeRef>(static_cast<uintptr_t>(texturePtr)));
    return reinterpret_cast<jlong>(new skgpu::graphite::BackendTexture(texture));
#else
    return 0;
#endif
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_gpu_graphite_BackendTextureKt__1nMakeVulkan(
        JNIEnv* env,
        jclass,
        jint width,
        jint height,
        jint imageLayout,
        jint queueFamilyIndex,
        jlong imagePtr,
        jintArray textureInfoValues) {
#if defined(SK_VULKAN)
    auto textureInfo = skikoVulkanTextureInfoFromIntArray(env, textureInfoValues);
    auto texture = skgpu::graphite::BackendTextures::MakeVulkan(
            SkISize::Make(width, height),
            textureInfo,
            static_cast<VkImageLayout>(imageLayout),
            static_cast<uint32_t>(queueFamilyIndex),
            reinterpret_cast<VkImage>(static_cast<uintptr_t>(imagePtr)),
            skgpu::VulkanAlloc());
    return reinterpret_cast<jlong>(new skgpu::graphite::BackendTexture(texture));
#else
    return 0;
#endif
}
