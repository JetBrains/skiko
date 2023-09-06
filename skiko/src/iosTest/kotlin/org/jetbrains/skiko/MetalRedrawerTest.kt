package org.jetbrains.skiko

import org.jetbrains.skia.Surface
import org.jetbrains.skiko.redrawer.MetalRedrawer
import platform.QuartzCore.CADisplayLink
import platform.QuartzCore.CAMetalLayer
import kotlin.native.internal.createCleaner
import kotlin.native.ref.WeakReference
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class MockNSRunLoop {
    val displayLinks = mutableListOf<CADisplayLink>()
}

@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
class MetalRedrawerTest {
    @Suppress("UNUSED", "UNUSED_PARAMETER")
    private class MetalRedrawerOwner(
        mockNSRunLoop: MockNSRunLoop
    ) {
        private val redrawer: MetalRedrawer

        init {
            val weakThis = WeakReference(this)

            redrawer = MetalRedrawer(
                CAMetalLayer(),
                drawCallback = { surface ->
                    weakThis.get()?.draw(surface)
                },
                addDisplayLinkToRunLoop = {
                    mockNSRunLoop.displayLinks.add(it)
                },
                disposeCallback = {
                    assertTrue(mockNSRunLoop.displayLinks.isNotEmpty(), "mockNSRunLoop.displayLinks must contain a displayLink")
                    assertTrue(mockNSRunLoop.displayLinks.remove(it.caDisplayLink))
                }
            )
        }

        @OptIn(ExperimentalStdlibApi::class)
        private val redrawerCleaner = createCleaner(redrawer) {
            it.dispose()
        }

        private fun draw(surface: Surface) = Unit
    }

    @Suppress("UNUSED_VARIABLE", "UNUSED")
    private fun createAndForgetMetalRedrawerOwner(mockNSRunLoop: MockNSRunLoop) {
        val owner = MetalRedrawerOwner(mockNSRunLoop)
    }

    @Test
    fun `check metal redrawer is disposed`() {
        val mockNSRunLoop = MockNSRunLoop()

        createAndForgetMetalRedrawerOwner(mockNSRunLoop)

        // GC can't sweep Objc-Kotlin objects in one pass due to different lifetime models
        // Two passes do not guarantee it either, this test can break in future
        kotlin.native.internal.GC.collect()
        kotlin.native.internal.GC.collect()

        assertTrue(mockNSRunLoop.displayLinks.isEmpty(), "displayLinks must be empty after MetalRedrawer is disposed. This test can be flaky and depends on assumptions about GC implementation.")
    }
}
