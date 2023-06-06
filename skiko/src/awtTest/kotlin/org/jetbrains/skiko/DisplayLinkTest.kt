package org.jetbrains.skiko

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.skiko.macos.DisplayLink
import org.jetbrains.skiko.macos.screenIDOf
import org.jetbrains.skiko.util.isUITestsEnabled
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.awt.Dimension
import javax.swing.JFrame

class DisplayLinkTest {
    @Test
    fun `await vsyncs`() = displayLinkTest { layer ->
        val screenID = screenIDOf(layer.windowHandle)
        val displayLink = DisplayLink(screenID)

        val refresh

        var frameCount = 0
        displayLink.await()
    }

    private fun displayLinkTest(
        block: suspend CoroutineScope.(layer: SkiaLayer) -> Unit
    ) = runBlocking(MainUIDispatcher) {
        assumeTrue(hostOs.isMacOS)
        assumeTrue(isUITestsEnabled)

        val layer = SkiaLayer()
        val frame = JFrame()
        try {
            frame.contentPane.add(layer)
            frame.size = Dimension(200, 200)
            frame.isVisible = true
            block(layer)
        } finally {
            layer.dispose()
            frame.dispose()
        }
    }
}