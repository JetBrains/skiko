package org.jetbrains.skiko.swing

import com.jetbrains.SharedTextures
import org.jetbrains.skia.Surface
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.awt.Image

internal class AcceleratedSwingPainter(
    internal val sharedTextures: SharedTexturesAdapter,
    private val fallbackPainterCreator: () -> SwingPainter,
) : SwingPainter {
    var imageWrapper: Image? = null
        private set

    var texturePtr: Long = 0L
        private set

    private var gc: GraphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .defaultScreenDevice.defaultConfiguration

    private var fallbackPainter: SwingPainter? = null

    override fun paint(g: Graphics2D, surface: Surface, texture: Long) {
        val deviceConfiguration = g.deviceConfiguration
        if (!deviceConfiguration.isSharedTextureCompatibleConfiguration()) {
            imageWrapper = null
            texturePtr = 0L
            if (fallbackPainter == null) fallbackPainter = fallbackPainterCreator()
            fallbackPainter?.paint(g, surface, texture)
            return
        }

        if (deviceConfiguration != gc || texturePtr != texture || imageWrapper == null) {
            gc = deviceConfiguration
            texturePtr = texture
            imageWrapper = sharedTextures.wrapTexture(gc, texturePtr)
        }

        g.drawImage(imageWrapper, 0, 0, null)
    }

    override fun dispose() {
        fallbackPainter?.dispose()
    }

    internal fun setCachedStateForTesting(imageWrapper: Image?, texturePtr: Long, gc: GraphicsConfiguration) {
        this.imageWrapper = imageWrapper
        this.texturePtr = texturePtr
        this.gc = gc
    }

    private fun GraphicsConfiguration.isSharedTextureCompatibleConfiguration(): Boolean =
        if (sharedTextures.textureType == SharedTextures.METAL_TEXTURE_TYPE) {
            javaClass.name == "sun.java2d.metal.MTLGraphicsConfig"
        } else {
            false
        }
}