package org.jetbrains.skia

import org.jetbrains.skiko.ExperimentalSkikoApi
import kotlin.jvm.JvmInline

/**
 * Interface for Vulkan device memory allocation used by Skia.
 *
 * Intended for integrations that already manage Vulkan memory themselves. Skiko does not
 * expose Vulkan APIs, so implementations are expected to call Vulkan through an external
 * binding library (e.g. LWJGL) using the raw handles passed to the methods below.
 *
 * If no allocator is provided, Skia's own allocator is used instead, which is the right
 * choice for most applications.
 *
 * @see [DirectContext.makeVulkan]
 */
@ExperimentalSkikoApi
abstract class VulkanMemoryAllocator {
    /**
     * Describes how Skia intends to use a buffer allocation.
     *
     * This may or may not be acknowledged by the underlying allocator implementation.
     */
    @JvmInline
    value class BufferUsage(val value: Int) {
        companion object {
            /**
             * GPU-only memory.
             */
            val GPU_ONLY = BufferUsage(0)
            /**
             * CPU-written, GPU-read memory.
             */
            val CPU_WRITES_GPU_READS = BufferUsage(1)
            /**
             * CPU-to-GPU transfer staging memory.
             */
            val TRANSFERS_FROM_CPU_TO_GPU = BufferUsage(2)
            /**
             * GPU-to-CPU transfer memory.
             */
            val TRANSFERS_FROM_GPU_TO_CPU = BufferUsage(3)
        }
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
    class Allocation(
        val deviceMemory: Long,
        val offset: Long,
        val size: Long,
        val memoryTypeIndex: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Allocation

            if (deviceMemory != other.deviceMemory) return false
            if (offset != other.offset) return false
            if (size != other.size) return false
            if (memoryTypeIndex != other.memoryTypeIndex) return false

            return true
        }

        override fun hashCode(): Int {
            var result = deviceMemory.hashCode()
            result = 31 * result + offset.hashCode()
            result = 31 * result + size.hashCode()
            result = 31 * result + memoryTypeIndex
            return result
        }

        override fun toString(): String {
            return "Allocation(deviceMemory=$deviceMemory, offset=$offset, size=$size, memoryTypeIndex=$memoryTypeIndex)"
        }
    }

    /**
     * Allocate device memory for [image].
     *
     * @param image Vulkan `VkImage` handle.
     * @param allocationPropertyFlags Bitmask of `ALLOCATION_FLAG_*` values.
     *
     * @return Allocation information, or `null` if allocation failed.
     */
    abstract fun allocateImageMemory(image: Long, allocationPropertyFlags: AllocationFlags): Allocation?

    /**
     * Allocate device memory for [buffer].
     *
     * @param buffer Vulkan `VkBuffer` handle.
     * @param usage Intended usage pattern for the buffer.
     * @param allocationPropertyFlags Bitmask of `ALLOCATION_FLAG_*` values.
     *
     * @return Allocation information, or `null` if allocation failed.
     */
    abstract fun allocateBufferMemory(buffer: Long, usage: BufferUsage, allocationPropertyFlags: AllocationFlags): Allocation?

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
        return allocateBufferMemory(buffer, BufferUsage(usageOrdinal), AllocationFlags(allocationPropertyFlags))
    }
}