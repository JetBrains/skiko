package org.jetbrains.skija.svg

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Managed.CleanerThunk
import org.jetbrains.skija.paragraph.Shadow
import org.jetbrains.skija.paragraph.TextBox
import org.jetbrains.skija.paragraph.Affinity
import org.jetbrains.skija.paragraph.Paragraph
import org.jetbrains.skija.paragraph.HeightMode
import org.jetbrains.skija.paragraph.StrutStyle
import org.jetbrains.skija.paragraph.BaselineMode
import org.jetbrains.skija.paragraph.RectWidthMode
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skija.paragraph.ParagraphCache
import org.jetbrains.skija.paragraph.ParagraphStyle
import org.jetbrains.skija.paragraph.RectHeightMode
import org.jetbrains.skija.paragraph.DecorationStyle
import org.jetbrains.skija.paragraph.ParagraphBuilder
import org.jetbrains.skija.paragraph.PlaceholderStyle
import org.jetbrains.skija.paragraph.TextStyleAttribute
import org.jetbrains.skija.paragraph.DecorationLineStyle
import org.jetbrains.skija.paragraph.PlaceholderAlignment
import org.jetbrains.skija.paragraph.PositionWithAffinity
import org.jetbrains.skija.paragraph.TypefaceFontProvider
import org.jetbrains.skija.shaper.Shaper
import org.jetbrains.skija.shaper.FontRun
import org.jetbrains.skija.shaper.LanguageRun
import org.jetbrains.skija.shaper.ShapingOptions
import org.jetbrains.skija.shaper.FontMgrRunIterator
import org.jetbrains.skija.shaper.IcuBidiRunIterator
import org.jetbrains.skija.shaper.ManagedRunIterator
import org.jetbrains.skija.shaper.HbIcuScriptRunIterator
import org.jetbrains.skija.shaper.TextBlobBuilderRunHandler
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import org.jetbrains.skija.skottie.Animation
import org.jetbrains.skija.sksg.InvalidationController
import org.jetbrains.skija.skottie.RenderFlag
import org.jetbrains.skija.skottie.AnimationBuilder
import org.jetbrains.skija.skottie.AnimationBuilderFlag
import org.jetbrains.skija.svg.SVGDOM
import org.jetbrains.skija.svg.SVGSVG
import org.jetbrains.skija.svg.SVGTag
import org.jetbrains.skija.svg.SVGNode
import org.jetbrains.skija.svg.SVGCanvas
import org.jetbrains.skija.svg.SVGLength
import org.jetbrains.skija.svg.SVGLengthType
import org.jetbrains.skija.svg.SVGLengthUnit
import org.jetbrains.skija.svg.SVGLengthContext
import org.jetbrains.skija.svg.SVGPreserveAspectRatio
import org.jetbrains.skija.svg.SVGPreserveAspectRatioAlign
import org.jetbrains.skija.svg.SVGPreserveAspectRatioScale
import org.jetbrains.skija.ColorFilter._LinearToSRGBGammaHolder
import org.jetbrains.skija.ColorFilter._SRGBToLinearGammaHolder
import org.jetbrains.skija.ColorFilter._LumaHolder
import org.jetbrains.skija.ColorSpace._SRGBHolder
import org.jetbrains.skija.ColorSpace._SRGBLinearHolder
import org.jetbrains.skija.ColorSpace._DisplayP3Holder
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.annotations.Contract
import org.jetbrains.skija.*
import org.jetbrains.skija.FontMgr._DefaultHolder
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats

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
    @Contract("_, _, _, _ -> new")
    fun make(bounds: Rect, out: WStream, convertTextToPaths: Boolean, prettyXML: Boolean): Canvas {
        Stats.onNativeCall()
        val ptr = _nMake(
            bounds._left,
            bounds._top,
            bounds._right,
            bounds._bottom,
            Native.Companion.getPtr(out),
            0 or (if (convertTextToPaths) 1 else 0) or if (prettyXML) 0 else 2
        )
        return org.jetbrains.skija.Canvas(ptr, true, out)
    }

    @ApiStatus.Internal
    external fun _nMake(left: Float, top: Float, right: Float, bottom: Float, wstreamPtr: Long, flags: Int): Long

    init {
        staticLoad()
    }
}