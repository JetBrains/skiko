package org.jetbrains.skiko.swing

import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.image.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import kotlin.math.*

// TODO: extract this code to a library and share it with JCEF implementation in IntelliJ
//  since this code is mostly taken from intellij repository with some small changes
internal class SwingOffscreenDrawer(
    private val swingLayerProperties: SwingLayerProperties
) {
    @Volatile
    private var volatileImage: VolatileImage? = null

    /**
     * Draws rendered image that is represented by [bytes] on [g].
     *
     * If size of the rendered image is bigger than size from [swingLayerProperties]
     * then only part of the image will be drawn on [g].
     *
     * @param g graphics where rendered picture given in [bytes] should be drawn
     * @param bytes bytes of rendered picture in little endian order
     * @param width width of rendered picture in real pixels
     * @param height height of rendered picture in real pixels
     */
    fun draw(g: Graphics2D, bytes: ByteArray, width: Int, height: Int) {
        val dirtyRectangles = listOf(
            Rectangle(0, 0, width, height)
        )
        val image = createImageFromBytes(bytes, width, height, dirtyRectangles)
        var vi = volatileImage

        do {
            if (vi == null || vi.width != swingLayerProperties.width || vi.height != swingLayerProperties.height) {
                vi = createVolatileImage(image)
            }
            drawVolatileImage(vi, image)
            when (vi.validate(swingLayerProperties.graphicsConfiguration)) {
                VolatileImage.IMAGE_RESTORED -> drawVolatileImage(vi, image)
                VolatileImage.IMAGE_INCOMPATIBLE -> vi = createVolatileImage(image)
            }
            g.drawImage(vi, 0, 0, null)
        } while (vi!!.contentsLost())

        volatileImage = vi
    }

    private fun createImageFromBytes(
        bytes: ByteArray,
        width: Int,
        height: Int,
        dirtyRectangles: List<Rectangle>
    ): BufferedImage {
        val src = ByteBuffer.wrap(bytes)
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE)
        val dstData = (image.raster.dataBuffer as DataBufferInt).data
        val srcData: IntBuffer = src.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer()
        for (rect in dirtyRectangles) {
            if (rect.width < image.width) {
                for (line in rect.y until rect.y + rect.height) {
                    val offset: Int = line * image.width + rect.x
                    srcData.position(offset)[dstData, offset, min(
                        rect.width.toDouble(),
                        (src.capacity() - offset).toDouble()
                    ).toInt()]
                }
            } else { // optimized for a buffer wide dirty rect
                val offset: Int = rect.y * image.width
                srcData.position(offset)[dstData, offset, min(
                    (rect.height * image.width).toDouble(),
                    (src.capacity() - offset).toDouble()
                ).toInt()]
            }
        }

        return image
    }

    private fun createVolatileImage(image: BufferedImage): VolatileImage {
        val vi = swingLayerProperties.graphicsConfiguration.createCompatibleVolatileImage(
            swingLayerProperties.width,
            swingLayerProperties.height,
            Transparency.TRANSLUCENT
        )
        drawVolatileImage(vi, image)
        return vi
    }

    private fun drawVolatileImage(vi: VolatileImage, image: BufferedImage) {
        val g = vi.graphics.create() as Graphics2D
        try {
            g.background = Color(0, 0, 0, 0)
            g.composite = AlphaComposite.Src
            g.clearRect(0, 0, swingLayerProperties.width, swingLayerProperties.height)
            val imageClipRectangle = Rectangle(0, 0, swingLayerProperties.width, swingLayerProperties.height)
            drawImage(g, image, sourceBounds = imageClipRectangle)
        } finally {
            g.dispose()
        }
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