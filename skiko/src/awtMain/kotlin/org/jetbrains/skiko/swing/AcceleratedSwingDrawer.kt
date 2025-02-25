package org.jetbrains.skiko.swing

import org.jetbrains.skia.Surface
import java.awt.Graphics2D
import com.jetbrains.JBR
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.awt.Image

class AcceleratedSwingDrawer : SwingDrawer {
    private val sharedTextures =
        if (JBR.isSharedTexturesSupported()) JBR.getSharedTextures()
        else throw UnsupportedOperationException("Shared textures are not supported")

    private var imageWrapper: Image? = null
    private var texturePtr: Long = 0L
    private var gc: GraphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .defaultScreenDevice.defaultConfiguration

    override fun draw(g: Graphics2D, surface: Surface, texture: Long) {
        if (g.deviceConfiguration != gc || texturePtr != texture || imageWrapper == null) {
            gc = g.deviceConfiguration
            texturePtr = texture
            imageWrapper = sharedTextures.wrapTexture(gc, texturePtr)
        }

        g.drawImage(imageWrapper, 0, 0, null)
    }

    override fun dispose() {
    }
}