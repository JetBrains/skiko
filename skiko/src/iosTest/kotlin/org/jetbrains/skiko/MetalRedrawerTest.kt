package org.jetbrains.skiko

import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.redrawer.MetalRedrawer
import platform.QuartzCore.CADisplayLink
import kotlin.native.internal.createCleaner
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

private val nsRunLoopMock = object {
    val displayLinks = mutableListOf<CADisplayLink>()
}
class MetalRedrawerTest {
    @OptIn(ExperimentalStdlibApi::class)
    private fun createAndForgetSkiaLayer(disposeCallback: () -> Unit) {
        // Current reference cycle looks like that
        // skiaLayer -> skikoView -> redrawer -> skiaLayer
        // redrawer creates CADisplayLink which is stored in global object (NSRunLoop) and used to strongly capture
        // an object referencing redrawer, creating a memory leak

        val skiaLayer = object : SkiaLayer() {
            // createCleaner can't capture anything
            // so we need to proxy call to disposeCallback via the cleaned object itself
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
    @Ignore // TODO: fails on CI due to Metal not supported, solve by extracting DisplayLink dispatch mechanism to separate class
    fun `check skia layer is disposed`() {
        var isDisposed = false

        createAndForgetSkiaLayer { isDisposed = true }

        // GC can't sweep Objc-Kotlin objects in one pass due to different lifetime models
        // Two passes do not guarantee it either, this test can break in future
        kotlin.native.internal.GC.collect()
        kotlin.native.internal.GC.collect()

        assertTrue(isDisposed)
    }
}