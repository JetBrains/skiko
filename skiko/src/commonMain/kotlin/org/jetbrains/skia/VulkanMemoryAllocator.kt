package org.jetbrains.skia

/**
 * Controls how Skia allocates Vulkan device memory.
 *
 * You may pass an implementation of this class to [DirectContext.makeVulkan].
 * When omitted, a built-in per-allocation allocator is used that is always functional, however,
 * it does not do any sub-allocations.
 */
abstract class VulkanMemoryAllocator {

    /**
     * Describes how Skia intends to use a buffer allocation.
     */
    enum class BufferUsage(internal val ordinalValue: Int) {
        /**
         * Accessed only on the GPU. This is always device local.
         */
        GPU_ONLY(0),

        /**
         * Frequently updated by the CPU, read by the GPU.
         */
        CPU_WRITES_GPU_READS(1),

        /**
         * Staging buffer for CPU-to-GPU transfers. Must be host-visible and host-coherent.
         */
        TRANSFERS_FROM_CPU_TO_GPU(2),

        /**
         * Buffer written by the GPU and read back on the CPU. Must be host-visible.
         */
        TRANSFERS_FROM_GPU_TO_CPU(3);
    }

    /**
     * The result of a successful allocation.
     *
     * @param deviceMemory    The allocated `VkDeviceMemory` handle (as a [Long]).
     * @param offset          Byte offset within [deviceMemory]; 0 for dedicated allocations.
     * @param size            Size of this allocation in bytes.
     * @param memoryTypeIndex Index into `VkPhysicalDeviceMemoryProperties.memoryTypes` that
     *                        was used. The allocator bridge uses this to determine coherency
     *                        and mappability flags for Skia.
     */
    data class Allocation(
        val deviceMemory: Long,
        val offset: Long,
        val size: Long,
        val memoryTypeIndex: Int
    )

    /**
     * Allocate device memory for [image].
     *
     * @param image                  `VkImage` handle (as [Long]) that needs memory.
     * @param allocationPropertyFlags Hints from Skia; combination of [ALLOCATION_FLAG_*] constants.
     */
    abstract fun allocateImageMemory(image: Long, allocationPropertyFlags: Int): Allocation?

    /**
     * Allocate device memory for [buffer].
     *
     * @param buffer                 `VkBuffer` handle (as [Long]) that needs memory.
     * @param usage                  How Skia intends to use this buffer — drives memory type choice.
     * @param allocationPropertyFlags Hints from Skia; combination of [ALLOCATION_FLAG_*] constants.
     */
    abstract fun allocateBufferMemory(buffer: Long, usage: BufferUsage, allocationPropertyFlags: Int): Allocation?

    /**
     * Map [deviceMemory] for CPU access and return the mapped address as a [Long],
     * or 0 if the memory cannot be or was not mapped.
     *
     * Only called for host-visible (mappable) memory. The default implementation returns 0.
     */
    open fun mapMemory(deviceMemory: Long): Long = 0L

    /**
     * Unmap [deviceMemory] previously returned by [mapMemory].
     */
    open fun unmapMemory(deviceMemory: Long) {}

    /**
     * Flush [size] bytes starting at [offset] within [deviceMemory] to make CPU writes
     * visible to the GPU.  Only called for non-coherent memory.
     *
     * @return a `VkResult` value (0 = `VK_SUCCESS`).
     */
    open fun flushMemory(deviceMemory: Long, offset: Long, size: Long): Int = 0

    /**
     * Invalidate [size] bytes starting at [offset] within [deviceMemory] so GPU writes
     * become visible to the CPU. Only called for non-coherent memory.
     *
     * @return a `VkResult` value (0 = `VK_SUCCESS`).
     */
    open fun invalidateMemory(deviceMemory: Long, offset: Long, size: Long): Int = 0

    /**
     * Free a [deviceMemory] handle previously returned by [allocateImageMemory] or
     * [allocateBufferMemory].
     */
    abstract fun freeMemory(deviceMemory: Long)

    /**
     * @return `(totalAllocated, totalUsed)` byte counts for diagnostic purposes.
     * Both may be 0 if tracking is not implemented.
     */
    open fun totalAllocatedAndUsedMemory(): Pair<Long, Long> = Pair(0L, 0L)

    @Suppress("unused")
    protected fun allocateBufferMemoryBridge(
        buffer: Long,
        usageOrdinal: Int,
        allocationPropertyFlags: Int
    ): Allocation? {
        val usage = BufferUsage.entries.getOrNull(usageOrdinal) ?: return null
        return allocateBufferMemory(buffer, usage, allocationPropertyFlags)
    }

    companion object {
        /**
         * Place allocation in its own `VkDeviceMemory`
         */
        const val ALLOCATION_FLAG_DEDICATED: Int = 0b0001

        /**
         * Lazily allocated and device-only, may be unavailable until accessed.
         */
        const val ALLOCATION_FLAG_LAZY: Int = 0b0010

        /**
         * Keep this memory persistently mapped (host-visible allocations only).
         */
        const val ALLOCATION_FLAG_PERSISTENTLY_MAPPED: Int = 0b0100

        /**
         * Allocate in a protected memory region.
         */
        const val ALLOCATION_FLAG_PROTECTED: Int = 0b1000
    }
}