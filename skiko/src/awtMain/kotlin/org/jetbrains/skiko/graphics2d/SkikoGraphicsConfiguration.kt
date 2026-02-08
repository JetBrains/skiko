/* ===============
 * SkijaGraphics2D
 * ===============
 *
 * (C)opyright 2021, by David Gilbert.
 *
 * The SkijaGraphics2D class has been developed by David Gilbert for
 * use with Orson Charts (http://www.object-refinery.com/orsoncharts) and
 * JFreeChart (http://www.jfree.org/jfreechart).  It may be useful for other
 * code that uses the Graphics2D API provided by Java2D.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of the Object Refinery Limited nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL OBJECT REFINERY LIMITED BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.jetbrains.skiko.graphics2d

import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.awt.image.DirectColorModel
import java.awt.image.VolatileImage

/**
 * A graphics configuration for the [SkikoGraphics2D] class.
 */
class SkikoGraphicsConfiguration
/**
 * Creates a new instance.
 *
 * @param width  the width of the bounds.
 * @param height  the height of the bounds.
 */(private val width: Int, private val height: Int) : GraphicsConfiguration() {
    private var device: GraphicsDevice? = null

    /**
     * Returns the graphics device that this configuration is associated with.
     *
     * @return The graphics device (never `null`).
     */
    override fun getDevice(): GraphicsDevice {
        if (device == null) {
            device = SkikoGraphicsDevice("SkijaGraphicsDevice", this)
        }
        return device!!
    }

    /**
     * Returns the color model for this configuration.
     *
     * @return The color model.
     */
    override fun getColorModel(): ColorModel? {
        return getColorModel(Transparency.TRANSLUCENT)
    }

    /**
     * Returns the color model for the specified transparency type, or
     * `null`.
     *
     * @param transparency  the transparency type.
     *
     * @return A color model (possibly `null`).
     */
    override fun getColorModel(transparency: Int): ColorModel? {
        return if (transparency == Transparency.TRANSLUCENT) {
            ColorModel.getRGBdefault()
        } else if (transparency == Transparency.OPAQUE) {
            DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff)
        } else {
            null
        }
    }

    /**
     * Returns the default transform.
     *
     * @return The default transform.
     */
    override fun getDefaultTransform(): AffineTransform {
        return AffineTransform()
    }

    /**
     * Returns the normalizing transform.
     *
     * @return The normalizing transform.
     */
    override fun getNormalizingTransform(): AffineTransform {
        return AffineTransform()
    }

    /**
     * Returns the bounds for this configuration.
     *
     * @return The bounds.
     */
    override fun getBounds(): Rectangle {
        return Rectangle(width, height)
    }

    private var img: BufferedImage? = null
    private var gc: GraphicsConfiguration? = null

    /**
     * Returns a volatile image.  This method is a workaround for a
     * ClassCastException that occurs on MacOSX when exporting a Swing UI
     * that uses the Nimbus Look and Feel.
     *
     * @param width  the image width.
     * @param height  the image height.
     * @param caps  the image capabilities.
     * @param transparency  the transparency.
     *
     * @return The volatile image.
     *
     * @throws AWTException if there is a problem creating the image.
     */
    @Throws(AWTException::class)
    override fun createCompatibleVolatileImage(
        width: Int, height: Int,
        caps: ImageCapabilities, transparency: Int
    ): VolatileImage {
        if (img == null) {
            img = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
            gc = img!!.createGraphics().deviceConfiguration
        }
        return gc!!.createCompatibleVolatileImage(
            width, height, caps,
            transparency
        )
    }
}