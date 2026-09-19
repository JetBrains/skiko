package org.jetbrains.skiko.sample.extensions

import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.WindowConstants
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val skiaLayer = SkiaLayer()
    val exitAfterMillis = exitAfterMillis(args)
    val player = loadSkottieAnimationPlayer()
    skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer) { canvas, width, height, _ ->
        player.render(canvas, width, height)
    }

    SwingUtilities.invokeLater {
        val window = JFrame(APP_NAME).apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            preferredSize = Dimension(640, 640)
        }

        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRender()
        window.pack()
        window.isVisible = true

        if (exitAfterMillis != null) {
            Timer(exitAfterMillis) {
                window.dispose()
                println(RENDER_SUCCESS_MESSAGE)
                exitProcess(0)
            }.apply {
                isRepeats = false
                start()
            }
        }
    }
}
