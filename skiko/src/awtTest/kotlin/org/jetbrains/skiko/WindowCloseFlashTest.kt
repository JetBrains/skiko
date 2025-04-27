package org.jetbrains.skiko

import kotlinx.coroutines.delay
import org.jetbrains.skiko.util.uiTest
import org.junit.Test
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension

class WindowCloseFlashTest {

    @Test
    fun `window flash on close`() = uiTest {
        lateinit var renderDelegate: SolidColorRenderer
        val window = UiTestWindow {
            size = Dimension(800, 800)
//            contentPane.add(HardwareLayer().also {
//                it.background = Color.BLACK
//            })
            renderDelegate = SolidColorRenderer(layer, Color.BLACK)
            layer.renderDelegate = renderDelegate
            contentPane.add(layer, BorderLayout.CENTER)
        }
//        val window = JFrame()

        window.isVisible = true

//        delay(500)
//        repeat(5) {
//            SwingUtilities.invokeLater {
//                debugPrint("needRedraw")
//                window.layer.needRedraw()
//            }
//            delay(1000)
//        }

        delay(2000)

//        (window.layer.redrawer as MetalRedrawer).debugResize(802, 802)
//        renderDelegate.continuousRedraw = false
//        delay(48)
        window.dispose()

        delay(500)
    }


}