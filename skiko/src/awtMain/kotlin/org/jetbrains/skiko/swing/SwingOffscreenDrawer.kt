package org.jetbrains.skiko.swing

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.BufferUtil
import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.image.*
import java.nio.ByteOrder
import java.nio.IntBuffer
import kotlin.math.*

internal class SwingOffscreenDrawer(
    private val swingLayerProperties: SwingLayerProperties
) {
    private var bufferedImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE)
    private var bitmap = Bitmap()

    fun draw(g: Graphics2D, surface: Surface) {
        val width = surface.width
        val height = surface.height
        if (bitmap.width != width || bitmap.height != height) {
            bitmap.allocPixelsFlags(ImageInfo.makeS32(width, height, ColorAlphaType.PREMUL), false)
        }

        surface.readPixels(bitmap, 0, 0)
        bufferedImage = createImageFromBytes(bitmap.peekPixels()!!.addr, width, height)
        drawImage(g, bufferedImage)
    }

    private fun createImageFromBytes(
        pBytes: Long,
        width: Int,
        height: Int,
    ): BufferedImage {
        if (bufferedImage.width != width || bufferedImage.height != height) {
            bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE)
        }
        val image = bufferedImage

        val dstData = (image.raster.dataBuffer as DataBufferInt).data
        val src = BufferUtil.getByteBufferFromPointer(pBytes, width * height * 4)
        val srcData: IntBuffer = src.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer()
        srcData.position(0).get(dstData, 0, min(image.height * image.width, srcData.capacity()))

        return image
    }

    private fun drawImage(
        g: Graphics,
        image: Image,
        x: Int = 0,
        y: Int = 0,
        dw: Int = -1,
        dh: Int = -1,
        sourceBounds: Rectangle? = null,
        op: BufferedImageOp? = null,
        observer: ImageObserver? = null
    ) {
        val hasDestinationSize = dw >= 0 && dh >= 0
        doDrawHiDpi(
            userWidth = swingLayerProperties.width,
            userHeight = swingLayerProperties.height,
            g = g,
            scale = swingLayerProperties.scale.toDouble(),
            dx = x,
            dy = y,
            dw = dw,
            dh = dh,
            hasDestinationSize = hasDestinationSize,
            op = op,
            image = image,
            srcBounds = sourceBounds,
            observer = observer
        )
    }

    @Suppress("NAME_SHADOWING")
    private fun doDrawHiDpi(
        userWidth: Int,
        userHeight: Int,
        g: Graphics,
        scale: Double,
        dx: Int,
        dy: Int,
        dw: Int,
        dh: Int,
        hasDestinationSize: Boolean,
        op: BufferedImageOp?,
        image: Image,
        srcBounds: Rectangle?,
        observer: ImageObserver?
    ) {
        var g1 = g
        var scale1 = scale
        var dx1 = dx
        var dy1 = dy
        var delta = 0.0
        // Calculate the delta based on the image size. The bigger the size - the smaller the delta.
        val maxSize = max(userWidth, userHeight)
        if (maxSize < Int.MAX_VALUE / 2) {
            var dotAccuracy = 1
            var pow: Double
            while (maxSize > 10.0.pow(dotAccuracy.toDouble()).also { pow = it }) {
                dotAccuracy++
            }
            delta = 1 / pow
        }

        val tx = (g1 as Graphics2D).transform
        var invG: Graphics2D? = null
        if ((tx.type and AffineTransform.TYPE_MASK_ROTATION) == 0 &&
            abs(scale1 - tx.scaleX) <= delta
        ) {
            scale1 = tx.scaleX

            // The image has the same original scale as the graphics scale. However, the real image
            // scale - userSize/realSize - can suffer from inaccuracy due to the image user size
            // rounding to int (userSize = (int)realSize/originalImageScale). This may case quality
            // loss if the image is drawn via Graphics.drawImage(image, <srcRect>, <dstRect>)
            // due to scaling in Graphics. To avoid that, the image should be drawn directly via
            // Graphics.drawImage(image, 0, 0) on the unscaled Graphics.
            val gScaleX = tx.scaleX
            val gScaleY = tx.scaleY
            tx.scale(1 / gScaleX, 1 / gScaleY)
            tx.translate(dx1 * gScaleX, dy1 * gScaleY)
            dy1 = 0
            dx1 = 0
            invG = g1.create() as Graphics2D
            g1 = invG
            invG.transform = tx
        }

        try {
            var dw = dw
            var dh = dh
            if (invG != null && hasDestinationSize) {
                dw = scaleSize(dw, scale1)
                dh = scaleSize(dh, scale1)
            }
            doDraw(
                op = op,
                image = image,
                invG = invG,
                hasDestinationSize = hasDestinationSize,
                dw = dw,
                dh = dh,
                sourceBounds = srcBounds,
                userWidth = userWidth,
                userHeight = userHeight,
                g = g1,
                dx = dx1,
                dy = dy1,
                observer = observer,
                scale = scale1
            )
        } finally {
            invG?.dispose()
        }
    }

    private fun scaleSize(size: Int, scale: Double) = (size * scale).roundToInt()

    @Suppress("NAME_SHADOWING")
    private fun doDraw(
        op: BufferedImageOp?,
        image: Image,
        invG: Graphics2D?,
        hasDestinationSize: Boolean,
        dw: Int,
        dh: Int,
        sourceBounds: Rectangle?,
        userWidth: Int,
        userHeight: Int,
        g: Graphics,
        dx: Int,
        dy: Int,
        observer: ImageObserver?,
        scale: Double
    ) {
        var image = image
        if (op != null && image is BufferedImage) {
            image = op.filter(image, null)
        }

        when {
            sourceBounds != null -> {
                fun size(size: Int) = scaleSize(size, scale)

                val sx = size(sourceBounds.x)
                val sy = size(sourceBounds.y)
                val sw = if (sourceBounds.width >= 0) size(sourceBounds.width) else size(userWidth) - sx
                val sh = if (sourceBounds.height >= 0) size(sourceBounds.height) else size(userHeight) - sy

                var dw = dw
                var dh = dh
                if (!hasDestinationSize) {
                    dw = size(userWidth)
                    dh = size(userHeight)
                }
                g.drawImage(/* img = */ image,
                    /* dx1 = */ dx, /* dy1 = */ dy, /* dx2 = */ dx + dw, /* dy2 = */ dy + dh,
                    /* sx1 = */ sx, /* sy1 = */ sy, /* sx2 = */ sx + sw, /* sy2 = */ sy + sh,
                    /* observer = */ observer
                )
            }

            hasDestinationSize -> {
                g.drawImage(image, dx, dy, dw, dh, observer)
            }

            invG == null -> {
                g.drawImage(image, dx, dy, userWidth, userHeight, observer)
            }

            else -> {
                g.drawImage(image, dx, dy, observer)
            }
        }
    }
}