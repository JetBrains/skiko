package org.jetbrains.skiko

import org.jetbrains.skiko.redrawer.*
import javax.swing.UIManager

actual fun setSystemLookAndFeel() = UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

internal actual fun makeDefaultRenderFactory(): RenderFactory =
    RenderFactory { layer, renderApi, analytics, properties ->
        val context = createRedrawer(layer, renderApi, analytics, properties)
        try {
            OnScreenRedrawer(layer, context)
        } catch (e: Throwable) {
            context.close()
            throw e
        }
    }
