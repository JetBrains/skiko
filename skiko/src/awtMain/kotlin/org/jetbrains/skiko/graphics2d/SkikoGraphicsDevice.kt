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

import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice

/**
 * A graphics device for SkijaGraphics2D.
 */
class SkikoGraphicsDevice
/**
 * Creates a new instance.
 *
 * @param id  the id.
 * @param defaultConfig  the default configuration.
 */(private val id: String, var defaultConfig: GraphicsConfiguration) : GraphicsDevice() {
    /**
     * Returns the device type.
     *
     * @return The device type.
     */
    override fun getType(): Int {
        return TYPE_RASTER_SCREEN
    }

    /**
     * Returns the id string (defined in the constructor).
     *
     * @return The id string.
     */
    override fun getIDstring(): String {
        return id
    }

    /**
     * Returns all configurations for this device.
     *
     * @return All configurations for this device.
     */
    override fun getConfigurations(): Array<GraphicsConfiguration> {
        return arrayOf(defaultConfiguration)
    }

    /**
     * Returns the default configuration for this device.
     *
     * @return The default configuration for this device.
     */
    override fun getDefaultConfiguration(): GraphicsConfiguration {
        return defaultConfig
    }
}