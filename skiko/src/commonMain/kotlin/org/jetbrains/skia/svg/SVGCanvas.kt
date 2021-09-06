@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.ExternalSymbolName
import kotlin.jvm.JvmStatic

object SVGCanvas {
    /**
     * Returns a new canvas that will generate SVG commands from its draw calls, and send
     * them to the provided stream. Ownership of the stream is not transfered, and it must
     * remain valid for the lifetime of the returned canvas.
     *
     * The canvas may buffer some drawing calls, so the output is not guaranteed to be valid
     * or complete until the canvas instance is deleted.
     *
     * @param bounds              defines an initial SVG viewport (viewBox attribute on the root SVG element).
     * @param out                 stream SVG commands will be written to
     * @return                    new Canvas
     */
    fun make(bounds: Rect, out: WStream): Canvas {
        return make(bounds, out, false, true)
    }

    /**
     * Returns a new canvas that will generate SVG commands from its draw calls, and send
     * them to the provided stream. Ownership of the stream is not transfered, and it must
     * remain valid for the lifetime of the returned canvas.
     *
     * The canvas may buffer some drawing calls, so the output is not guaranteed to be valid
     * or complete until the canvas instance is deleted.
     *
     * @param bounds              defines an initial SVG viewport (viewBox attribute on the root SVG element).
     * @param out                 stream SVG commands will be written to
     * @param convertTextToPaths  emit text as &lt;path&gt;s
     * @param prettyXML           add newlines and tabs in output
     * @return                    new Canvas
     */
    fun make(bounds: Rect, out: WStream, convertTextToPaths: Boolean, prettyXML: Boolean): Canvas {
        Stats.onNativeCall()
        val ptr = _nMake(
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.bottom,
            Native.getPtr(out),
            0 or (if (convertTextToPaths) 1 else 0) or if (prettyXML) 0 else 2
        )
        return Canvas(ptr, true, out)
    }

    @JvmStatic
    @ExternalSymbolName("org_jetbrains_skia_svg_SVGCanvas__1nMake")
    external fun _nMake(left: Float, top: Float, right: Float, bottom: Float, wstreamPtr: Long, flags: Int): Long

    init {
        staticLoad()
    }
}