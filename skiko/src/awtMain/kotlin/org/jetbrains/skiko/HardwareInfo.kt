package org.jetbrains.skiko

object HardwareInfo {
    init {
        Library.load()
    }

    /**
     * Returns the preferred GPU that will be used by Skiko to render graphical content.
     *
     * [priority] which GPU will be chosen first. By default, it reads skiko.gpu.priority system property.
     *
     * If a GPU isn't supported by Skiko, the next GPU will be chosen. If no GPU is supported, returns null.
     */
    fun preferredGpu(priority: GpuPriority = SkikoProperties.gpuPriority): Gpu? =
        getPreferredGpuName(priority.ordinal)?.let(::Gpu)

    data class Gpu(val name: String)
}

private external fun getPreferredGpuName(priority: Int): String?


