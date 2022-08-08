package org.jetbrains.skiko

import org.junit.Test

class HardwareInfoTest {
    @Test
    fun `get gpu info`() {
        println(HardwareInfo.preferredGpu())
        println("Discrete preferred: " + HardwareInfo.preferredGpu(GpuPriority.Discrete))
        println("Integrated preferred: " + HardwareInfo.preferredGpu(GpuPriority.Integrated))
    }
}