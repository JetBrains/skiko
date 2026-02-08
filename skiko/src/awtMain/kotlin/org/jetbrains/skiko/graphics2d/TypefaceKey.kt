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

import org.jetbrains.skia.FontStyle

/**
 * A key used to identify a `Typeface` in a map used to cache items.
 */
class TypefaceKey
/**
 * Creates a new key.
 *
 * @param fontName  the font name.
 * @param style  the style.
 */(
    /**
     * Returns the font name.
     *
     * @return The font name.
     */
    val fontName: String,
    /**
     * Returns the font style.
     *
     * @return The font style.
     */
    val style: FontStyle
) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as TypefaceKey
        return if (fontName != that.fontName) false else style == that.style
    }

    override fun hashCode(): Int {
        var result = fontName.hashCode()
        result = 31 * result + style.hashCode()
        return result
    }
}