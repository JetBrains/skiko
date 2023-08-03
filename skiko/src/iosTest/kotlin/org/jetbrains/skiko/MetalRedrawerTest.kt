package org.jetbrains.skiko

import org.jetbrains.skia.Surface
import org.jetbrains.skiko.redrawer.MetalRedrawer
import platform.QuartzCore.CADisplayLink
import platform.QuartzCore.CAMetalLayer
import kotlin.native.internal.createCleaner
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

private val nsRunLoopMock = object {
    val displayLinks = mutableListOf<CADisplayLink>()
}
class MetalRedrawerTest {
    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("UNUSED_VARIABLE", "UNUSED", "UNUSED_PARAMETER")
    private fun createAndForgetMetalRedrawerOwner(disposeCallback: () -> Unit) {
        val metalRedrawerOwner = object {
            /*
             * [createCleaner] can't capture anything so we need to proxy call to [disposeCallback]
             * via the cleaned object itself.
             */
            val disposeCallbackProxy = object {
                /*
                 * It will be cleaned as soon, as [metalRedrawerOwner] is GCed.
                 */
                fun dispose() {
                    disposeCallback()
                }
            }

            val cleaner = createCleaner(disposeCallbackProxy) {
                it.dispose()
            }

            val redrawer = MetalRedrawer(
                CAMetalLayer(),
                addDisplayLinkToRunLoop = {
                    nsRunLoopMock.displayLinks.add(it)
                }
            ) {
                // Cross-reference metalRedrawerOwner<->metalRedrawer, metalRedrawer is captured strongly by global object [nsRunLoopMock]
                this.drawIntoSurface(it)
            }

            fun drawIntoSurface(surface: Surface) = Unit
        }
    }

    @Test
    // TODO: uncomment @Ignore when gradle test creating environment without Metal support is fixed
    // @Ignore
    fun `check metal redrawer is disposed`() {
        var isDisposed = false

        createAndForgetMetalRedrawerOwner { isDisposed = true }

        // GC can't sweep Objc-Kotlin objects in one pass due to different lifetime models
        // Two passes do not guarantee it either, this test can break in future
        kotlin.native.internal.GC.collect()
        kotlin.native.internal.GC.collect()

        assertTrue(isDisposed)
    }
}