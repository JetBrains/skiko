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

import org.jetbrains.skiko.Logger
import java.awt.Font
import java.awt.FontMetrics

/**
 * Returns font metrics.
 */
class SkikoFontMetrics(skijaFont: org.jetbrains.skia.Font?, awtFont: Font?) :
    FontMetrics(awtFont) {
    /** Skija font.  */
    private val skijaFont: org.jetbrains.skia.Font?

    /** Skija font metrics.  */
    private val metrics: org.jetbrains.skia.FontMetrics

    /**
     * Creates a new instance.
     *
     * @param skijaFont  the Skija font (`null` not permitted).
     * @param awtFont  the AWT font (`null` not permitted).
     */
    init {
        metrics = skijaFont!!.metrics
        this.skijaFont = skijaFont
    }

    /**
     * Returns the leading.
     *
     * @return The leading.
     */
    override fun getLeading(): Int {
        val result = metrics.leading.toInt()
        Logger.debug { "getLeading() -> $result" }
        return result
    }

    /**
     * Returns the ascent for the font.
     *
     * @return The ascent.
     */
    override fun getAscent(): Int {
        val result = -metrics.ascent.toInt()
        Logger.debug { "getAscent() -> $result" }
        return result
    }

    /**
     * Returns the descent for the font.
     *
     * @return The descent.
     */
    override fun getDescent(): Int {
        val result = metrics.descent.toInt()
        Logger.debug {
            "getDescent() -> $result"
        }
        return result
    }

    /**
     * Returns the width of the specified character.
     *
     * @param ch  the character.
     *
     * @return The width.
     */
    override fun charWidth(ch: Char): Int {
        val result = skijaFont!!.measureTextWidth(Character.toString(ch)).toInt()
        Logger.debug { "charWidth($ch) -> $result" }
        return result
    }

    /**
     * Returns the width of a character sequence.
     *
     * @param data  the characters.
     * @param off  the offset.
     * @param len  the length.
     *
     * @return The width of the character sequence.
     */
    override fun charsWidth(data: CharArray, off: Int, len: Int): Int {
        val result = skijaFont!!.measureTextWidth(String(data, off, len)).toInt()
        Logger.debug { "charsWidth($data, $off, $len) -> $result" }
        return result
    }
}