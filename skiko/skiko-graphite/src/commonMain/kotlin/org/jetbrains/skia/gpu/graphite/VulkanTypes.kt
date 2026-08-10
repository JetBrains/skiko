package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skiko.ExperimentalSkikoApi
import kotlin.jvm.JvmInline

/**
 * Wrapper for Vulkan image format values (`VkFormat`).
 *
 * References the standard Vulkan specification format enum values.
 */
@ExperimentalSkikoApi
@JvmInline
value class VulkanFormat(val value: Int) {
    companion object {
        /** `VK_FORMAT_B8G8R8A8_SRGB = 50` */
        val B8G8R8A8_SRGB = VulkanFormat(50)
    }
}

/**
 * Wrapper for Vulkan image usage bitmask flags (`VkImageUsageFlags` / `VkImageUsageFlagBits`).
 *
 * References the standard Vulkan specification image usage flags.
 */
@ExperimentalSkikoApi
@JvmInline
value class VulkanImageUsageFlags(val value: Int) {
    infix fun or(other: VulkanImageUsageFlags): VulkanImageUsageFlags =
        VulkanImageUsageFlags(this.value or other.value)

    companion object {
        /** `VK_IMAGE_USAGE_TRANSFER_SRC_BIT = 0x00000001` */
        val TRANSFER_SRC = VulkanImageUsageFlags(0x00000001)

        /** `VK_IMAGE_USAGE_TRANSFER_DST_BIT = 0x00000002` */
        val TRANSFER_DST = VulkanImageUsageFlags(0x00000002)

        /** `VK_IMAGE_USAGE_SAMPLED_BIT = 0x00000004` */
        val SAMPLED = VulkanImageUsageFlags(0x00000004)

        /** `VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT = 0x00000010` */
        val COLOR_ATTACHMENT = VulkanImageUsageFlags(0x00000010)

        /** `VK_IMAGE_USAGE_INPUT_ATTACHMENT_BIT = 0x00000080` */
        val INPUT_ATTACHMENT = VulkanImageUsageFlags(0x00000080)
    }
}


/**
 * Wrapper for Vulkan image tiling layout modes (`VkImageTiling`).
 *
 * References the standard Vulkan specification image tiling modes.
 */
@ExperimentalSkikoApi
@JvmInline
value class VulkanImageTiling(val value: Int) {
    companion object {
        /** `VK_IMAGE_TILING_OPTIMAL = 0` */
        val OPTIMAL = VulkanImageTiling(0)
    }
}

/**
 * Wrapper for Vulkan buffer/image sharing modes (`VkSharingMode`).
 *
 * References the standard Vulkan specification sharing modes across queue families.
 */
@ExperimentalSkikoApi
@JvmInline
value class VulkanSharingMode(val value: Int) {
    companion object {
        /** `VK_SHARING_MODE_EXCLUSIVE = 0` */
        val EXCLUSIVE = VulkanSharingMode(0)
    }
}

/**
 * Wrapper for Vulkan image aspect flags (`VkImageAspectFlags` / `VkImageAspectFlagBits`).
 *
 * References the standard Vulkan specification aspect mask bits.
 */
@ExperimentalSkikoApi
@JvmInline
value class VulkanImageAspectFlags(val value: Int) {
    infix fun or(other: VulkanImageAspectFlags): VulkanImageAspectFlags =
        VulkanImageAspectFlags(this.value or other.value)

    companion object {
        /** `VK_IMAGE_ASPECT_COLOR_BIT = 0x00000001` */
        val COLOR = VulkanImageAspectFlags(0x00000001)
    }
}

/**
 * Wrapper for Vulkan image creation flag bitmask (`VkImageCreateFlags` / `VkImageCreateFlagBits`).
 *
 * References the standard Vulkan specification image creation flags.
 */
@ExperimentalSkikoApi
@JvmInline
value class VulkanImageCreateFlags(val value: Int) {
    infix fun or(other: VulkanImageCreateFlags): VulkanImageCreateFlags =
        VulkanImageCreateFlags(this.value or other.value)

    companion object {
        val NONE = VulkanImageCreateFlags(0)
    }
}

/**
 * Descriptor containing Vulkan-specific image information for Graphite backend textures.
 *
 * @param format Vulkan image pixel format (`VkFormat`).
 * @param imageUsageFlags Vulkan image usage flags (`VkImageUsageFlags`).
 * @param sampleCount Number of samples per pixel (1 for non-MSAA).
 * @param mipmapped `true` if mipmaps are present/allocated for this texture.
 * @param flags Vulkan image creation flags (`VkImageCreateFlags`).
 * @param imageTiling Vulkan image tiling layout (`VkImageTiling`).
 * @param sharingMode Vulkan queue sharing mode (`VkSharingMode`).
 * @param aspectMask Vulkan image aspect mask (`VkImageAspectFlags`).
 */
@ExperimentalSkikoApi
class VulkanTextureInfo(
    val format: VulkanFormat,
    val imageUsageFlags: VulkanImageUsageFlags,
    val sampleCount: Int = 1,
    val mipmapped: Boolean = false,
    val flags: VulkanImageCreateFlags = VulkanImageCreateFlags.NONE,
    val imageTiling: VulkanImageTiling = VulkanImageTiling.OPTIMAL,
    val sharingMode: VulkanSharingMode = VulkanSharingMode.EXCLUSIVE,
    val aspectMask: VulkanImageAspectFlags = VulkanImageAspectFlags.COLOR,
) {
    internal fun packToIntArray(): IntArray = intArrayOf(
        format.value,
        imageUsageFlags.value,
        sampleCount,
        if (mipmapped) 1 else 0,
        flags.value,
        imageTiling.value,
        sharingMode.value,
        aspectMask.value,
    )
}
