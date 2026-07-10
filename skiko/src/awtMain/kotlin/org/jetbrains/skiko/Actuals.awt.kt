package org.jetbrains.skiko

import org.jetbrains.skiko.rendercontext.*
import javax.swing.UIManager

actual fun setSystemLookAndFeel() = UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

internal actual fun makeDefaultRenderFactory(): RenderFactory =
    RenderFactory { layer, renderApi, analytics, properties ->
        val context = createRenderContext(layer, renderApi, analytics, properties)
        try {
            OnScreenRenderer(layer, context)
        } catch (e: Throwable) {
            context.close()
            throw e
        }
    }
