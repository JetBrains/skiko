package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.redrawer.MetalRedrawer
import platform.QuartzCore.CADisplayLink
import kotlin.native.internal.createCleaner
import kotlin.test.Test
import kotlin.test.assertEquals

private val nsRunLoopMock = object {
    val displayLinks = mutableListOf<CADisplayLink>()
}
class MetalRedrawerTest {
    @OptIn(ExperimentalStdlibApi::class)
    private fun createAndForgetRedrawer(disposeCallback: () -> Unit) {
        val skiaLayer = object : SkiaLayer() {
            val disposeCallbackProxy = object {
                fun dispose() {
                    disposeCallback()
                }
            }

            val cleaner = createCleaner(disposeCallbackProxy) {
                it.dispose()
            }
        }

        skiaLayer.skikoView = object : SkikoView {
            val redrawer = MetalRedrawer(skiaLayer) {
                nsRunLoopMock.displayLinks.add(it)
            }

            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) = Unit
        }
    }

    @Test
    fun `check redrawer is disposed`() {
        var isDisposed = false

        createAndForgetRedrawer { isDisposed = true }

        // GC can't sweep Objc-Kotlin objects in one pass due to different lifetime models
        kotlin.native.internal.GC.collect()
        kotlin.native.internal.GC.collect()

        // not needed yet
        // reachabilityBarrier(globalDisplayLinksStorageMimickingRunLoop)

        assertEquals(true, isDisposed)
    }
}