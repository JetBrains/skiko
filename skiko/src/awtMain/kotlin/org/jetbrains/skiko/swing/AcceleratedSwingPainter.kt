package org.jetbrains.skiko.swing

import org.jetbrains.skia.Surface
import java.awt.Graphics2D
import com.jetbrains.JBR
import com.jetbrains.SharedTextures
import org.jetbrains.skiko.RenderException
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.awt.Image

internal class AcceleratedSwingPainter : SwingPainter {
    private val sharedTextures =
        if (JBR.isSharedTexturesSupported() &&
            JBR.getSharedTextures().textureType == SharedTextures.METAL_TEXTURE_TYPE
        ) JBR.getSharedTextures()
        else throw RenderException("Shared textures are not supported")

    private var imageWrapper: Image? = null
    private var texturePtr: Long = 0L
    private var gc: GraphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .defaultScreenDevice.defaultConfiguration

    override fun paint(g: Graphics2D, surface: Surface, texture: Long) {
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
