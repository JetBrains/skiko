package org.jetbrains.skiko

import org.jetbrains.skia.Surface
import org.jetbrains.skiko.redrawer.MetalRedrawer
import org.jetbrains.skiko.redrawer.SurfaceDrawer
import platform.QuartzCore.CADisplayLink
import platform.QuartzCore.CAMetalLayer
import kotlin.native.internal.createCleaner
import kotlin.native.ref.WeakReference
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

private val nsRunLoopMock = object {
    val displayLinks = mutableListOf<CADisplayLink>()
}
class MetalRedrawerTest {
    @Suppress("UNUSED", "UNUSED_PARAMETER")
    private class MetalRedrawerOwner(disposeCallback: () -> Unit) {
        private val redrawer = MetalRedrawer(
            CAMetalLayer(),
            WeakReference(object : SurfaceDrawer {
                override fun draw(surface: Surface) {
                    this@MetalRedrawerOwner.draw(surface)
                }
            }),
            addDisplayLinkToRunLoop = {
                nsRunLoopMock.displayLinks.add(it)
            },
            onDispose = {
                disposeCallback()
            }
        )

        @OptIn(ExperimentalStdlibApi::class)
        private val redrawerCleaner = createCleaner(redrawer) {
            it.dispose()
        }

        private fun draw(surface: Surface) = Unit
    }

    @Suppress("UNUSED_VARIABLE", "UNUSED")
    private fun createAndForgetMetalRedrawerOwner(disposeCallback: () -> Unit) {
        val owner = MetalRedrawerOwner(disposeCallback)
    }

    @Test
    // TODO: remove @Ignore when gradle creating test environment without Metal support is fixed
    @Ignore
    fun `check metal redrawer is disposed`() {
        var isDisposed = false

        createAndForgetMetalRedrawerOwner { isDisposed = true }

        // GC can't sweep Objc-Kotlin objects in one pass due to different lifetime models
        // Two passes do not guarantee it either, this test can break in future
        kotlin.native.internal.GC.collect()
        kotlin.native.internal.GC.collect()
        kotlin.native.internal.GC.collect()
        kotlin.native.internal.GC.collect()

        assertTrue(isDisposed)
    }
}