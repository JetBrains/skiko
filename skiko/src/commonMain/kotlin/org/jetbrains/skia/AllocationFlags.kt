package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class AllocationFlags(val bits: Int) {
    companion object {
        /**
         * Requests a dedicated `VkDeviceMemory` allocation.
         */
        val DEDICATED = AllocationFlags(0b0001)
        /**
         * Requests lazily allocated device‑local memory.
         */
        val LAZY = AllocationFlags(0b0010)
        /**
         * Requests persistent mapping for host‑visible memory.
         */
        val PERSISTENTLY_MAPPED = AllocationFlags(0b0100)
        /**
         * Requests protected memory allocation.
         */
        val PROTECTED = AllocationFlags(0b1000)
    }
}