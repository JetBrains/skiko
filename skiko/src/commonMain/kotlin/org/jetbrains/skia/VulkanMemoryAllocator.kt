package org.jetbrains.skia

/**
 * Interface for Vulkan device memory allocation used by Skia.
 *
 * @see [DirectContext.makeVulkan]
 */
abstract class VulkanMemoryAllocator {

    /**
     * Describes how Skia intends to use a buffer allocation.
     *
     * This may or may not be acknowledged by the underlying allocator implementation.
     */
    enum class BufferUsage(internal val ordinalValue: Int) {
        /**
         * GPU-only memory.
         */
        GPU_ONLY(0),

        /**
         * CPU-written, GPU-read memory.
         */
        CPU_WRITES_GPU_READS(1),

        /**
         * CPU-to-GPU transfer staging memory.
         */
        TRANSFERS_FROM_CPU_TO_GPU(2),

        /**
         * GPU-to-CPU transfer memory.
         */
        TRANSFERS_FROM_GPU_TO_CPU(3);
    }

    /**
     * The result of a successful allocation.
     *
     * @property deviceMemory Vulkan `VkDeviceMemory` handle.
     * @property offset Byte offset within `deviceMemory`.
     *                  For dedicated allocations this is typically `0`.
     * @property size Allocation size in bytes.
     * @property memoryTypeIndex Index into
     * `VkPhysicalDeviceMemoryProperties.memoryTypes`.
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
     * @param image Vulkan `VkImage` handle.
     * @param allocationPropertyFlags Bitmask of `ALLOCATION_FLAG_*` values.
     *
     * @return Allocation information, or `null` if allocation failed.
     */
    abstract fun allocateImageMemory(image: Long, allocationPropertyFlags: Int): Allocation?

    /**
     * Allocate device memory for [buffer].
     *
     * @param buffer Vulkan `VkBuffer` handle.
     * @param usage Intended usage pattern for the buffer.
     * @param allocationPropertyFlags Bitmask of `ALLOCATION_FLAG_*` values.
     *
     * @return Allocation information, or `null` if allocation failed.
     */
    abstract fun allocateBufferMemory(buffer: Long, usage: BufferUsage, allocationPropertyFlags: Int): Allocation?

    /**
     * Maps a Vulkan memory allocation for CPU access.
     *
     * Only called for host-visible memory types.
     *
     * @param deviceMemory Vulkan `VkDeviceMemory` handle.
     *
     * @return Mapped pointer address as a `Long`, or `0` on failure.
     */
    open fun mapMemory(deviceMemory: Long): Long = 0L

    /**
     * Unmaps a previously mapped Vulkan memory allocation.
     *
     * @param deviceMemory Vulkan `VkDeviceMemory` handle.
     */
    open fun unmapMemory(deviceMemory: Long) {}

    /**
     * Flushes CPU writes for a non-coherent memory range.
     *
     * Called only for non-coherent host-visible memory.
     *
     * @param deviceMemory Vulkan `VkDeviceMemory` handle.
     * @param offset Byte offset of the flushed range.
     * @param size Size of the flushed range in bytes.
     *
     * @return Vulkan `VkResult`.
     */
    open fun flushMemory(deviceMemory: Long, offset: Long, size: Long): Int = 0

    /**
     * Invalidates a non-coherent memory range.
     *
     * Makes GPU writes visible to the CPU.
     * Called only for non-coherent host-visible memory.
     *
     * @param deviceMemory Vulkan `VkDeviceMemory` handle.
     * @param offset Byte offset of the invalidated range.
     * @param size Size of the invalidated range in bytes.
     *
     * @return Vulkan `VkResult`.
     */
    open fun invalidateMemory(deviceMemory: Long, offset: Long, size: Long): Int = 0

    /**
     * Frees a Vulkan memory allocation.
     *
     * @param deviceMemory Vulkan `VkDeviceMemory` handle previously returned
     * by this allocator.
     */
    abstract fun freeMemory(deviceMemory: Long)

    /**
     * Returns allocator memory statistics.
     *
     * @return Pair of:
     * - total allocated bytes
     * - total used bytes
     *
     * Implementations that do not track statistics may return `(0, 0)`.
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
         * Requests a dedicated `VkDeviceMemory` allocation.
         */
        const val ALLOCATION_FLAG_DEDICATED: Int = 0b0001

        /**
         * Requests lazily allocated device-local memory.
         */
        const val ALLOCATION_FLAG_LAZY: Int = 0b0010

        /**
         * Requests persistent mapping for host-visible memory.
         */
        const val ALLOCATION_FLAG_PERSISTENTLY_MAPPED: Int = 0b0100

        /**
         * Requests protected memory allocation.
         */
        const val ALLOCATION_FLAG_PROTECTED: Int = 0b1000
    }
}