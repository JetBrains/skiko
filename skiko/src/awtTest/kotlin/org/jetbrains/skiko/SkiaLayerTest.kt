package org.jetbrains.skiko

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.*
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.context.JvmContextHandler
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.tests.runTest
import org.jetbrains.skiko.util.ScreenshotTestRule
import org.jetbrains.skiko.util.UiTestScope
import org.jetbrains.skiko.util.UiTestWindow
import org.jetbrains.skiko.util.uiTest
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JLayeredPane
import javax.swing.WindowConstants
import kotlin.random.Random
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("BlockingMethodInNonBlockingContext", "SameParameterValue")
class SkiaLayerTest {
    private val fontCollection = FontCollection()
        .setDefaultFontManager(FontMgr.default)

    private fun paragraph(size: Float, text: String) =
        ParagraphBuilder(ParagraphStyle(), fontCollection)
            .pushStyle(
                TextStyle().apply {
                    color = Color.RED.rgb
                    fontSize = size
                }
            )
            .addText(text)
            .popStyle()
            .build()

    @get:Rule
    val screenshots = ScreenshotTestRule()

    @Test
    fun `should not leak native windows`() = uiTest {
        assumeTrue(hostOs.isMacOS)

        suspend fun createAndDisposeWindow() {
            val layer = SkiaLayer()
            val frame = JFrame()
            frame.contentPane.add(layer)
            frame.size = Dimension(200, 200)
            frame.isVisible = true
            delay(30)
            layer.dispose()
            frame.dispose()
        }

        // warm caches
        repeat(8) {
            createAndDisposeWindow()
        }

        delay(1000)
        val initialWindowCount = getApplicationWindowCount()

        repeat(32) {
            createAndDisposeWindow()
        }

        delay(1000)
        val actualWindowCount = getApplicationWindowCount()

        assertTrue(
            initialWindowCount >= actualWindowCount,
            "initialWindowCount=$initialWindowCount, actualWindowCount=$actualWindowCount"
        )
    }

    private val bytes = byteArrayOf(
        67, 111, 109, 112, 111, 115, 101, 32, 49, 46, 48, 46, 48, 45, 114, 99, 49, 50, 44, 32, 79, 112, 101, 110, 74, 68, 75, 32, 49, 53, 44, 32, 87, 105, 110, 100, 111, 119, 115, 32, 49, 49, 13, 10, 13, 10, 80, 101, 114, 102, 111, 114, 109, 32, 105, 110, 32, 91, 116, 101, 109, 112, 108, 97, 116, 101, 115, 47, 100, 101, 115, 107, 116, 111, 112, 45, 116, 101, 109, 112, 108, 97, 116, 101, 93, 40, 104, 116, 116, 112, 115, 58, 47, 47, 103, 105, 116, 104, 117, 98, 46, 99, 111, 109, 47, 74, 101, 116, 66, 114, 97, 105, 110, 115, 47, 99, 111, 109, 112, 111, 115, 101, 45, 106, 98, 47, 116, 114, 101, 101, 47, 109, 97, 115, 116, 101, 114, 47, 116, 101, 109, 112, 108, 97, 116, 101, 115, 47, 100, 101, 115, 107, 116, 111, 112, 45, 116, 101, 109, 112, 108, 97, 116, 101, 41, 58, 13, 10, 96, 96, 96, 13, 10, 103, 114, 97, 100, 108, 101, 119, 32, 114, 117, 110, 68, 105, 115, 116, 114, 105, 98, 117, 116, 97, 98, 108, 101, 13, 10, 96, 96, 96, 13, 10, 13, 10, 82, 101, 115, 117, 108, 116, 58, 13, 10, 96, 96, 96, 13, 10, 69, 120, 99, 101, 112, 116, 105, 111, 110, 32, 105, 110, 32, 116, 104, 114, 101, 97, 100, 32, 34, 109, 97, 105, 110, 34, 32, 106, 97, 118, 97, 46, 97, 119, 116, 46, 65, 87, 84, 69, 114, 114, 111, 114, 58, 32, 65, 115, 115, 105, 115, 116, 105, 118, 101, 32, 84, 101, 99, 104, 110, 111, 108, 111, 103, 121, 32, 110, 111, 116, 32, 102, 111, 117, 110, 100, 58, 32, 99, 111, 109, 46, 115, 117, 110, 46, 106, 97, 118, 97, 46, 97, 99, 99, 101, 115, 115, 105, 98, 105, 108, 105, 116, 121, 46, 65, 99, 99, 101, 115, 115, 66, 114, 105, 100, 103, 101, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 100, 101, 115, 107, 116, 111, 112, 47, 106, 97, 118, 97, 46, 97, 119, 116, 46, 84, 111, 111, 108, 107, 105, 116, 46, 110, 101, 119, 65, 87, 84, 69, 114, 114, 111, 114, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 100, 101, 115, 107, 116, 111, 112, 47, 106, 97, 118, 97, 46, 97, 119, 116, 46, 84, 111, 111, 108, 107, 105, 116, 46, 102, 97, 108, 108, 98, 97, 99, 107, 84, 111, 76, 111, 97, 100, 67, 108, 97, 115, 115, 70, 111, 114, 65, 84, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 115, 116, 114, 101, 97, 109, 46, 70, 111, 114, 69, 97, 99, 104, 79, 112, 115, 36, 70, 111, 114, 69, 97, 99, 104, 79, 112, 36, 79, 102, 82, 101, 102, 46, 97, 99, 99, 101, 112, 116, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 115, 116, 114, 101, 97, 109, 46, 82, 101, 102, 101, 114, 101, 110, 99, 101, 80, 105, 112, 101, 108, 105, 110, 101, 36, 50, 36, 49, 46, 97, 99, 99, 101, 112, 116, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 72, 97, 115, 104, 77, 97, 112, 36, 75, 101, 121, 83, 112, 108, 105, 116, 101, 114, 97, 116, 111, 114, 46, 102, 111, 114, 69, 97, 99, 104, 82, 101, 109, 97, 105, 110, 105, 110, 103, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 115, 116, 114, 101, 97, 109, 46, 65, 98, 115, 116, 114, 97, 99, 116, 80, 105, 112, 101, 108, 105, 110, 101, 46, 99, 111, 112, 121, 73, 110, 116, 111, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 115, 116, 114, 101, 97, 109, 46, 65, 98, 115, 116, 114, 97, 99, 116, 80, 105, 112, 101, 108, 105, 110, 101, 46, 119, 114, 97, 112, 65, 110, 100, 67, 111, 112, 121, 73, 110, 116, 111, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 115, 116, 114, 101, 97, 109, 46, 70, 111, 114, 69, 97, 99, 104, 79, 112, 115, 36, 70, 111, 114, 69, 97, 99, 104, 79, 112, 46, 101, 118, 97, 108, 117, 97, 116, 101, 83, 101, 113, 117, 101, 110, 116, 105, 97, 108, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 115, 116, 114, 101, 97, 109, 46, 70, 111, 114, 69, 97, 99, 104, 79, 112, 115, 36, 70, 111, 114, 69, 97, 99, 104, 79, 112, 36, 79, 102, 82, 101, 102, 46, 101, 118, 97, 108, 117, 97, 116, 101, 83, 101, 113, 117, 101, 110, 116, 105, 97, 108, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 115, 116, 114, 101, 97, 109, 46, 65, 98, 115, 116, 114, 97, 99, 116, 80, 105, 112, 101, 108, 105, 110, 101, 46, 101, 118, 97, 108, 117, 97, 116, 101, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 115, 116, 114, 101, 97, 109, 46, 82, 101, 102, 101, 114, 101, 110, 99, 101, 80, 105, 112, 101, 108, 105, 110, 101, 46, 102, 111, 114, 69, 97, 99, 104, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 100, 101, 115, 107, 116, 111, 112, 47, 106, 97, 118, 97, 46, 97, 119, 116, 46, 84, 111, 111, 108, 107, 105, 116, 46, 108, 111, 97, 100, 65, 115, 115, 105, 115, 116, 105, 118, 101, 84, 101, 99, 104, 110, 111, 108, 111, 103, 105, 101, 115, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 100, 101, 115, 107, 116, 111, 112, 47, 106, 97, 118, 97, 46, 97, 119, 116, 46, 84, 111, 111, 108, 107, 105, 116, 46, 103, 101, 116, 68, 101, 102, 97, 117, 108, 116, 84, 111, 111, 108, 107, 105, 116, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 100, 101, 115, 107, 116, 111, 112, 47, 106, 97, 118, 97, 120, 46, 115, 119, 105, 110, 103, 46, 85, 73, 77, 97, 110, 97, 103, 101, 114, 46, 60, 99, 108, 105, 110, 105, 116, 62, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 111, 114, 103, 46, 106, 101, 116, 98, 114, 97, 105, 110, 115, 46, 115, 107, 105, 107, 111, 46, 83, 101, 116, 117, 112, 46, 105, 110, 105, 116, 40, 83, 101, 116, 117, 112, 46, 107, 116, 58, 50, 53, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 111, 114, 103, 46, 106, 101, 116, 98, 114, 97, 105, 110, 115, 46, 115, 107, 105, 107, 111, 46, 83, 101, 116, 117, 112, 46, 105, 110, 105, 116, 36, 100, 101, 102, 97, 117, 108, 116, 40, 83, 101, 116, 117, 112, 46, 107, 116, 58, 54, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 111, 114, 103, 46, 106, 101, 116, 98, 114, 97, 105, 110, 115, 46, 115, 107, 105, 107, 111, 46, 76, 105, 98, 114, 97, 114, 121, 46, 108, 111, 97, 100, 40, 76, 105, 98, 114, 97, 114, 121, 46, 107, 116, 58, 57, 50, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 111, 114, 103, 46, 106, 101, 116, 98, 114, 97, 105, 110, 115, 46, 115, 107, 105, 97, 46, 105, 109, 112, 108, 46, 76, 105, 98, 114, 97, 114, 121, 36, 67, 111, 109, 112, 97, 110, 105, 111, 110, 46, 115, 116, 97, 116, 105, 99, 76, 111, 97, 100, 40, 76, 105, 98, 114, 97, 114, 121, 46, 106, 118, 109, 46, 107, 116, 58, 49, 50, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 97, 110, 100, 114, 111, 105, 100, 120, 46, 99, 111, 109, 112, 111, 115, 101, 46, 117, 105, 46, 67, 111, 110, 102, 105, 103, 117, 114, 101, 83, 119, 105, 110, 103, 71, 108, 111, 98, 97, 108, 115, 70, 111, 114, 67, 111, 109, 112, 111, 115, 101, 95, 100, 101, 115, 107, 116, 111, 112, 75, 116, 46, 99, 111, 110, 102, 105, 103, 117, 114, 101, 83, 119, 105, 110, 103, 71, 108, 111, 98, 97, 108, 115, 70, 111, 114, 67, 111, 109, 112, 111, 115, 101, 40, 67, 111, 110, 102, 105, 103, 117, 114, 101, 83, 119, 105, 110, 103, 71, 108, 111, 98, 97, 108, 115, 70, 111, 114, 67, 111, 109, 112, 111, 115, 101, 46, 100, 101, 115, 107, 116, 111, 112, 46, 107, 116, 58, 52, 57, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 97, 110, 100, 114, 111, 105, 100, 120, 46, 99, 111, 109, 112, 111, 115, 101, 46, 117, 105, 46, 67, 111, 110, 102, 105, 103, 117, 114, 101, 83, 119, 105, 110, 103, 71, 108, 111, 98, 97, 108, 115, 70, 111, 114, 67, 111, 109, 112, 111, 115, 101, 95, 100, 101, 115, 107, 116, 111, 112, 75, 116, 46, 99, 111, 110, 102, 105, 103, 117, 114, 101, 83, 119, 105, 110, 103, 71, 108, 111, 98, 97, 108, 115, 70, 111, 114, 67, 111, 109, 112, 111, 115, 101, 36, 100, 101, 102, 97, 117, 108, 116, 40, 67, 111, 110, 102, 105, 103, 117, 114, 101, 83, 119, 105, 110, 103, 71, 108, 111, 98, 97, 108, 115, 70, 111, 114, 67, 111, 109, 112, 111, 115, 101, 46, 100, 101, 115, 107, 116, 111, 112, 46, 107, 116, 58, 51, 56, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 97, 110, 100, 114, 111, 105, 100, 120, 46, 99, 111, 109, 112, 111, 115, 101, 46, 117, 105, 46, 119, 105, 110, 100, 111, 119, 46, 65, 112, 112, 108, 105, 99, 97, 116, 105, 111, 110, 95, 100, 101, 115, 107, 116, 111, 112, 75, 116, 46, 97, 112, 112, 108, 105, 99, 97, 116, 105, 111, 110, 40, 65, 112, 112, 108, 105, 99, 97, 116, 105, 111, 110, 46, 100, 101, 115, 107, 116, 111, 112, 46, 107, 116, 58, 55, 54, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 77, 97, 105, 110, 75, 116, 46, 109, 97, 105, 110, 40, 109, 97, 105, 110, 46, 107, 116, 58, 50, 55, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 77, 97, 105, 110, 75, 116, 46, 109, 97, 105, 110, 40, 109, 97, 105, 110, 46, 107, 116, 41, 13, 10, 67, 97, 117, 115, 101, 100, 32, 98, 121, 58, 32, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 67, 108, 97, 115, 115, 78, 111, 116, 70, 111, 117, 110, 100, 69, 120, 99, 101, 112, 116, 105, 111, 110, 58, 32, 99, 111, 109, 46, 115, 117, 110, 46, 106, 97, 118, 97, 46, 97, 99, 99, 101, 115, 115, 105, 98, 105, 108, 105, 116, 121, 46, 65, 99, 99, 101, 115, 115, 66, 114, 105, 100, 103, 101, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 100, 107, 46, 105, 110, 116, 101, 114, 110, 97, 108, 46, 108, 111, 97, 100, 101, 114, 46, 66, 117, 105, 108, 116, 105, 110, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 46, 108, 111, 97, 100, 67, 108, 97, 115, 115, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 100, 107, 46, 105, 110, 116, 101, 114, 110, 97, 108, 46, 108, 111, 97, 100, 101, 114, 46, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 115, 36, 65, 112, 112, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 46, 108, 111, 97, 100, 67, 108, 97, 115, 115, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 46, 108, 111, 97, 100, 67, 108, 97, 115, 115, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 67, 108, 97, 115, 115, 46, 102, 111, 114, 78, 97, 109, 101, 48, 40, 78, 97, 116, 105, 118, 101, 32, 77, 101, 116, 104, 111, 100, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 97, 116, 32, 106, 97, 118, 97, 46, 98, 97, 115, 101, 47, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 67, 108, 97, 115, 115, 46, 102, 111, 114, 78, 97, 109, 101, 40, 85, 110, 107, 110, 111, 119, 110, 32, 83, 111, 117, 114, 99, 101, 41, 13, 10, 32, 32, 32, 32, 32, 32, 32, 32, 46, 46, 46, 32, 50, 50, 32, 109, 111, 114, 101, 13, 10, 96, 96, 96)

    @Test
    fun nativeCrashTest() = uiTest {
        val fc = FontCollection()
        fc.setDefaultFontManager(FontMgr.default)

        val style = ParagraphStyle().apply {
            height = 1f
            heightMode = HeightMode.ALL
            maxLinesCount = -1
            textIndent = TextIndent(0f, 0f)
            direction = Direction.LTR
            alignment = Alignment.START
            strutStyle = StrutStyle().apply {
                fontStyle = FontStyle.NORMAL
                fontSize = 14f
                height = 1f
                leading = -1f
            }
            textStyle = TextStyle().apply {
                letterSpacing = 1.4f
                fontSize = 32.0f
                fontStyle = FontStyle.NORMAL
                color = Color.BLACK.rgb
            }
        }
        val p = ParagraphBuilder(
            style = style,
            fc = fc
        ).use {
            it.addText(String(bytes))
            it.build()
        }.layout(1576f)

        val window = UiTestWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(2000, 1500)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.layer.skikoView = object : SkikoView {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    p.paint(canvas,0f,0f)
                }
            }
            window.isUndecorated = true
            window.isVisible = true

            delay(2000)
        } finally {
            window.close()
        }

        val r = p.getGlyphPositionAtCoordinate(1300f, 1020f)
        println("R = $r")
    }

    @Test
    fun `render single window`() = uiTest {
        val window = UiTestWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val app = RectRenderer(window.layer, 200, 100, Color.RED)
            window.layer.skikoView = app
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            screenshots.assert(window.bounds, "frame1")

            app.rectWidth = 100
            window.layer.needRedraw()
            delay(1000)
            screenshots.assert(window.bounds, "frame2")
        } finally {
            window.close()
        }
    }

    @Test
    fun `render single window before window show`() = uiTest {
        val window = UiTestWindow()
        try {
            window.setLocation(200, 200)
            window.preferredSize = Dimension(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val app = RectRenderer(window.layer, 200, 100, Color.RED)
            window.layer.skikoView = app
            window.isUndecorated = true
            window.pack()
            window.paint(window.graphics)
            window.isVisible = true

            delay(1000)
            screenshots.assert(window.bounds, "frame1")

            app.rectWidth = 100
            window.layer.needRedraw()
            delay(1000)
            screenshots.assert(window.bounds, "frame2")
        } finally {
            window.close()
        }
    }

    @Test
    fun `render empty layer`() = uiTest {
        val window = JFrame()
        val layer = SkiaLayer(
            properties = SkiaLayerProperties(renderApi = renderApi)
        )
        var renderedWidth = -1
        layer.skikoView = object : SkikoView {
            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                renderedWidth = width
            }
        }
        layer.size = Dimension(0, 0)
        val density = window.graphicsConfiguration.defaultTransform.scaleX
        try {
            val panel = JLayeredPane()
            panel.add(layer)
            window.contentPane.add(panel)
            window.setLocation(200, 200)
            window.size = Dimension(200, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.isUndecorated = true
            window.isVisible = true
            layer.needRedraw()
            delay(1000)
            assertEquals(0, renderedWidth)

            renderedWidth = -1
            layer.needRedraw()
            delay(1000)
            assertEquals(0, renderedWidth)

            renderedWidth = -1
            layer.size = Dimension(30, 40)
            layer.needRedraw()
            delay(1000)
            assertEquals((30 * density).toInt(), renderedWidth)

            renderedWidth = -1
            layer.size = Dimension(0, 0)
            layer.needRedraw()
            delay(1000)
            assertEquals(0, renderedWidth)
        } finally {
            layer.dispose()
            window.close()
        }
    }

    @Test
    fun `resize window`() = uiTest {
        val window = UiTestWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(40, 20)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.layer.skikoView = RectRenderer(window.layer, 20, 10, Color.RED)
            window.isUndecorated = true
            window.isVisible = true
            delay(1000)

            window.setSize(80, 40)
            delay(1000)

            screenshots.assert(window.bounds)
        } finally {
            window.close()
        }
    }

    @Test
    fun `render three windows`() = uiTest {
        fun window(color: Color) = UiTestWindow().apply {
            setLocation(200, 200)
            setSize(400, 200)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.skikoView = RectRenderer(layer, 200, 100, color)
            isUndecorated = true
            isVisible = true
        }

        val window1 = window(Color.RED)
        val window2 = window(Color.GREEN)
        val window3 = window(Color.BLACK)

        try {
            delay(1000)

            window1.toFront()
            delay(1000)
            screenshots.assert(window1.bounds, "window1")

            window2.toFront()
            delay(1000)
            screenshots.assert(window2.bounds, "window2")

            window3.toFront()
            delay(1000)
            screenshots.assert(window3.bounds, "window3")
        } finally {
            window1.close()
            window2.close()
            window3.close()
        }
    }

    @Test
    fun `should call onRender after init, after resize, and only once after needRedraw`() = uiTest {
        var renderCount = 0

        val window = UiTestWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(40, 20)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.layer.skikoView = object : SkikoView {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    renderCount++
                }
            }
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            assertTrue(renderCount > 0)
            renderCount = 0

            window.setSize(50, 20)
            delay(1000)
            assertTrue(renderCount > 0)
            renderCount = 0

            window.layer.needRedraw()
            delay(1000)
            assertEquals(1, renderCount)
        } finally {
            window.close()
        }
    }

    @Test(timeout = 60000)
    fun `stress test - open multiple windows`() = uiTest {
        fun window(isAnimated: Boolean) = UiTestWindow().apply {
            setLocation(200, 200)
            setSize(40, 20)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.skikoView = if (isAnimated) {
                AnimatedBoxRenderer(layer, pixelsPerSecond = 20.0, size = 20.0)
            } else {
                RectRenderer(layer, 20, 10, Color.RED)
            }
            isUndecorated = true
            isVisible = true
        }

        val random = Random(31415926)
        val openedWindows = mutableListOf<UiTestWindow>()

        repeat(10) {
            val needOpen = random.nextDouble() > 0.5f

            repeat(10) {
                if (needOpen) {
                    val window = window(isAnimated = random.nextDouble() > 0.5f)
                    openedWindows.add(window)
                } else if (openedWindows.size > 0) {
                    val index = (random.nextDouble() * (openedWindows.size - 1)).toInt()
                    openedWindows.removeAt(index).close()
                }
            }

            val delayCount = random.nextLong(5)
            if (delayCount > 0) {
                delay(delayCount * 10)
            }
        }

        openedWindows.forEach(JFrame::close)

        delay(5000)
    }

    @Test(timeout = 60000)
    fun `stress test - resize and paint immediately`() = uiTest {
        fun openWindow() = UiTestWindow(
            properties = SkiaLayerProperties(isVsyncEnabled = false, isVsyncFramelimitFallbackEnabled = true)
        ).apply {
            setLocation(200, 200)
            setSize(400, 200)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.skikoView = AnimatedBoxRenderer(layer, pixelsPerSecond = 20.0, size = 20.0)
            isVisible = true
        }

        val window = openWindow()

        repeat(100) {
            window.size = Dimension(200 + Random.nextInt(200), 200 + Random.nextInt(200))
            window.paint(window.graphics)
            yield()
        }

        window.close()
    }

    @Test(timeout = 60000)
    fun `stress test - open and paint immediately`() = uiTest {
        fun openWindow() = UiTestWindow(
            properties = SkiaLayerProperties(isVsyncEnabled = false, isVsyncFramelimitFallbackEnabled = true)
        ).apply {
            setLocation(200, 200)
            setSize(400, 200)
            preferredSize = Dimension(400, 200)
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            layer.skikoView = object : SkikoView {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                }
            }
        }

        repeat(30) {
            delay(100)
            val window = openWindow()
            window.isVisible = true
            window.layer.needRedraw()
            yield()
            window.paint(window.graphics)
            window.close()
        }
    }

    @Test(timeout = 60000)
    fun `fallback to software renderer, fail on init context`() = uiTest {
        testFallbackToSoftware(
            object : RenderFactory {
                override fun createRedrawer(
                    layer: SkiaLayer,
                    renderApi: GraphicsApi,
                    analytics: SkiaLayerAnalytics,
                    properties: SkiaLayerProperties
                ) = object : Redrawer {
                    private val contextHandler = object : JvmContextHandler(layer) {
                        override fun initContext() = false
                        override fun initCanvas() = Unit
                    }

                    override fun dispose() = Unit
                    override fun needRedraw() = Unit
                    override fun redrawImmediately() = layer.inDrawScope(contextHandler::draw)
                    override val renderInfo = ""
                }
            }
        )
    }

    @Test(timeout = 60000)
    fun `fallback to software renderer, fail on create redrawer`() = uiTest {
        testFallbackToSoftware(
            object : RenderFactory {
                override fun createRedrawer(
                    layer: SkiaLayer,
                    renderApi: GraphicsApi,
                    analytics: SkiaLayerAnalytics,
                    properties: SkiaLayerProperties
                ) = throw RenderException()
            }
        )
    }

    @Test(timeout = 60000)
    fun `fallback to software renderer, fail on draw`() = uiTest {
        testFallbackToSoftware(
            object : RenderFactory {
                override fun createRedrawer(
                    layer: SkiaLayer,
                    renderApi: GraphicsApi,
                    analytics: SkiaLayerAnalytics,
                    properties: SkiaLayerProperties
                ) = object : Redrawer {
                    override fun dispose() = Unit
                    override fun needRedraw() = Unit
                    override fun redrawImmediately() = layer.inDrawScope {
                        throw RenderException()
                    }
                    override val renderInfo = ""
                }
            }
        )
    }

    private suspend fun UiTestScope.testFallbackToSoftware(nonSoftwareRenderFactory: RenderFactory) {
        val window = UiTestWindow(
            renderFactory = OverrideNonSoftwareRenderFactory(nonSoftwareRenderFactory)
        )
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val app = RectRenderer(window.layer, 200, 100, Color.RED)
            window.layer.skikoView = app
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            screenshots.assert(window.bounds, "frame1", "testFallbackToSoftware")

            app.rectWidth = 100
            window.layer.needRedraw()
            delay(1000)
            screenshots.assert(window.bounds, "frame2", "testFallbackToSoftware")

            assertEquals(GraphicsApi.SOFTWARE_COMPAT, window.layer.renderApi)
        } finally {
            window.close()
        }
    }

    private class OverrideNonSoftwareRenderFactory(
        private val nonSoftwareRenderFactory: RenderFactory
    ) : RenderFactory {
        override fun createRedrawer(
            layer: SkiaLayer,
            renderApi: GraphicsApi,
            analytics: SkiaLayerAnalytics,
            properties: SkiaLayerProperties
        ): Redrawer {
            return if (renderApi == GraphicsApi.SOFTWARE_COMPAT) {
                RenderFactory.Default.createRedrawer(layer, renderApi, analytics, properties)
            } else {
                nonSoftwareRenderFactory.createRedrawer(layer, renderApi, analytics, properties)
            }
        }
    }

    @Test(timeout = 20000)
    fun `render continuously empty content without vsync`() = uiTest {
        val targetDrawCount = 500
        var drawCount = 0
        val onDrawCompleted = CompletableDeferred<Unit>()

        val window = UiTestWindow(
            properties = SkiaLayerProperties(
                isVsyncEnabled = false,
                isVsyncFramelimitFallbackEnabled = true
            )
        )

        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            window.layer.skikoView = object : SkikoView {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    drawCount++

                    if (drawCount < targetDrawCount) {
                        window.layer.needRedraw()
                    } else {
                        onDrawCompleted.complete(Unit)
                    }
                }
            }
            window.isUndecorated = true
            window.isVisible = true

            onDrawCompleted.await()
        } finally {
            window.close()
        }
    }

    @Test
    fun `render text (Windows)`() {
        testRenderText(OS.Windows)
    }

    @Test
    fun `render text (Linux)`() {
        testRenderText(OS.Linux)
    }

    @Test
    fun `render text (MacOS)`() {
        testRenderText(OS.MacOS)
    }

    @Test
    fun analytics() = uiTest {
        val analytics = object : SkiaLayerAnalytics {
            val rendererInfo = object {
                var skikoVersion: String? = null
                var os: OS? = null
                var api: GraphicsApi? = null

                var init = 0
                var deviceChosen = 0
            }

            val deviceInfo = object {
                var skikoVersion: String? = null
                var os: OS? = null
                var api: GraphicsApi? = null
                var deviceName: String? = null

                var init = 0
                var contextInit = 0
                var beforeFirstFrameRender = 0
                var afterFirstFrameRender = 0
            }

            @ExperimentalSkikoApi
            override fun renderer(
                skikoVersion: String,
                os: OS,
                api: GraphicsApi
            ) = object : SkiaLayerAnalytics.RendererAnalytics {
                init {
                    rendererInfo.skikoVersion = skikoVersion
                    rendererInfo.os = os
                    rendererInfo.api = api
                    rendererInfo.init = 0
                    rendererInfo.deviceChosen = 0
                }

                override fun init() {
                    rendererInfo.init++
                }

                override fun deviceChosen() {
                    rendererInfo.deviceChosen++
                }
            }

            @ExperimentalSkikoApi
            override fun device(
                skikoVersion: String,
                os: OS,
                api: GraphicsApi,
                deviceName: String?
            ) = object : SkiaLayerAnalytics.DeviceAnalytics {
                init {
                    deviceInfo.skikoVersion = skikoVersion
                    deviceInfo.os = os
                    deviceInfo.api = api
                    deviceInfo.deviceName = deviceName

                    deviceInfo.init = 0
                    deviceInfo.contextInit = 0
                    deviceInfo.beforeFirstFrameRender = 0
                    deviceInfo.afterFirstFrameRender = 0
                }

                override fun init() {
                    deviceInfo.init++
                }

                override fun contextInit() {
                    deviceInfo.contextInit++
                }

                override fun beforeFirstFrameRender() {
                    deviceInfo.beforeFirstFrameRender++
                }

                override fun afterFirstFrameRender() {
                    deviceInfo.afterFirstFrameRender++
                }
            }
        }

        val window = UiTestWindow(analytics = analytics)
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val app = RectRenderer(window.layer, 200, 100, Color.RED)
            window.layer.skikoView = app
            window.isUndecorated = true
            window.isVisible = true

            delay(1000)
            assertEquals(Version.skiko, analytics.rendererInfo.skikoVersion)
            assertEquals(hostOs, analytics.rendererInfo.os)
            assertNotNull(analytics.rendererInfo.api)
            assertEquals(1, analytics.rendererInfo.init)
            assertEquals(1, analytics.rendererInfo.deviceChosen)

            assertEquals(Version.skiko, analytics.deviceInfo.skikoVersion)
            assertNotNull(analytics.deviceInfo.api)
            assertNotNull(analytics.deviceInfo.deviceName)
            assertEquals(1, analytics.deviceInfo.init)
            assertEquals(1, analytics.deviceInfo.contextInit)
            assertEquals(1, analytics.deviceInfo.beforeFirstFrameRender)
            assertEquals(1, analytics.deviceInfo.afterFirstFrameRender)
        } finally {
            window.close()
        }
    }

    private fun testRenderText(os: OS) = uiTest {
        assumeTrue(hostOs == os)

        val window = UiTestWindow()
        try {
            window.setLocation(200, 200)
            window.setSize(400, 200)
            window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

            val paragraph by lazy { paragraph(window.layer.contentScale * 40, "=-+Нп") }

            window.layer.skikoView = object : SkikoView {
                override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                    canvas.clear(Color.WHITE.rgb)
                    paragraph.layout(Float.POSITIVE_INFINITY)
                    paragraph.paint(canvas, 0f, 0f)
                }
            }

            window.isUndecorated = true
            window.isVisible = true
            delay(1000)

            // check the line metrics
            val lineMetrics = paragraph.lineMetrics
            assertTrue(lineMetrics.isNotEmpty())
            assertEquals(0, lineMetrics.first().startIndex)
            assertEquals(5, lineMetrics.first().endIndex)
            assertEquals(5, lineMetrics.first().endExcludingWhitespaces)
            assertEquals(5, lineMetrics.first().endIncludingNewline)
            assertEquals(true, lineMetrics.first().isHardBreak)
            assertEquals(0, lineMetrics.first().lineNumber)

            screenshots.assert(window.bounds)
        } finally {
            window.close()
        }
    }

    private class RectRenderer(
        private val layer: SkiaLayer,
        var rectWidth: Int,
        var rectHeight: Int,
        private val rectColor: Color
    ) : SkikoView {
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            val dpi = layer.contentScale
            canvas.drawRect(Rect(0f, 0f, width.toFloat(), height.toFloat()), Paint().apply {
                color = Color.WHITE.rgb
            })
            canvas.drawRect(Rect(0f, 0f, rectWidth * dpi, rectHeight * dpi), Paint().apply {
                color = rectColor.rgb
            })
        }
    }

    private class AnimatedBoxRenderer(
        private val layer: SkiaLayer,
        private val pixelsPerSecond: Double,
        private val size: Double
    ) : SkikoView {
        private var oldNanoTime = Long.MAX_VALUE
        private var x = 0.0

        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            canvas.clear(Color.WHITE.rgb)

            val dt = (nanoTime - oldNanoTime).coerceAtLeast(0) / 1E9
            oldNanoTime = nanoTime

            x += dt * pixelsPerSecond
            if (x - size > width) {
                x = 0.0
            }

            canvas.drawRect(Rect(x.toFloat(), 0f, x.toFloat() + size.toFloat(), size.toFloat()), Paint().apply {
                color = Color.RED.rgb
            })

            layer.needRedraw()
        }
    }
}

private fun JFrame.close() = dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
