package org.jetbrains.skia

import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skiko.Library
import kotlin.test.Test
import kotlin.test.assertFailsWith

class VulkanInteropTest {
    private companion object {
        init {
            Library.load()
        }
    }

    @Test
    fun `makeVulkan validates required handles`() {
        assertFailsWith<IllegalArgumentException> {
            DirectContext.makeVulkan(
                instancePtr = NullPointer,
                physicalDevicePtr = 1L,
                devicePtr = 2L,
                queuePtr = 3L,
                graphicsQueueIndex = 0,
                instanceProcAddr = 4L,
                deviceProcAddr = 5L,
                apiVersion = 1
            )
        }
    }

    @Test
    fun `makeVulkan backend render target validates required arguments`() {
        assertFailsWith<IllegalArgumentException> {
            BackendRenderTarget.makeVulkan(
                width = 1,
                height = 1,
                imagePtr = NullPointer,
                imageTiling = 0,
                imageLayout = 0,
                format = 0,
                imageUsageFlags = 0,
                sampleCnt = 1,
                levelCnt = 1
            )
        }
    }
}
