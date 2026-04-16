package org.jetbrains.skiko.swing

import com.jetbrains.SharedTextures
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.skia.Surface
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.awt.Image

internal class AcceleratedSwingPainter(
    private val fallback: SwingPainter,
    @VisibleForTesting internal val sharedTextures: SharedTexturesAdapter
) : SwingPainter {
    private var imageWrapper: Image? = null
    private var texturePtr: Long = 0L
    private var gc: GraphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .defaultScreenDevice.defaultConfiguration

    override fun paint(g: Graphics2D, surface: Surface, texture: Long) {
        val deviceConfiguration = g.deviceConfiguration
        if (!deviceConfiguration.isSharedTextureCompatibleConfiguration()) {
            imageWrapper = null
            texturePtr = 0L
            fallback.paint(g, surface, texture)
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
        fallback.dispose()
    }

    @VisibleForTesting
    internal fun setCachedStateForTesting(imageWrapper: Image?, texturePtr: Long, gc: GraphicsConfiguration) {
        this.imageWrapper = imageWrapper
        this.texturePtr = texturePtr
        this.gc = gc
    }

    @get:VisibleForTesting
    internal val imageWrapperForTesting: Image?
        get() = imageWrapper

    @get:VisibleForTesting
    internal val texturePtrForTesting: Long
        get() = texturePtr

    private fun GraphicsConfiguration.isSharedTextureCompatibleConfiguration(): Boolean =
        if (sharedTextures.textureType == SharedTextures.METAL_TEXTURE_TYPE) {
            javaClass.name == "sun.java2d.metal.MTLGraphicsConfig"
        } else {
            false
        }
}
