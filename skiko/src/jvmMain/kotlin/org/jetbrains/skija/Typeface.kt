package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Managed.CleanerThunk
import org.jetbrains.skija.paragraph.Shadow
import org.jetbrains.skija.paragraph.TextBox
import org.jetbrains.skija.paragraph.Affinity
import org.jetbrains.skija.ManagedString
import org.jetbrains.skija.paragraph.Paragraph
import org.jetbrains.skija.IRange
import org.jetbrains.skija.FontFeature
import org.jetbrains.skija.Typeface
import org.jetbrains.skija.paragraph.HeightMode
import org.jetbrains.skija.paragraph.StrutStyle
import org.jetbrains.skija.paragraph.BaselineMode
import org.jetbrains.skija.paragraph.RectWidthMode
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skija.FontMgr
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
import org.jetbrains.skija.TextBlob
import org.jetbrains.skija.shaper.FontRun
import org.jetbrains.skija.FourByteTag
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
import org.jetbrains.skija.Matrix33
import org.jetbrains.skija.svg.SVGDOM
import org.jetbrains.skija.svg.SVGSVG
import org.jetbrains.skija.svg.SVGTag
import org.jetbrains.skija.svg.SVGNode
import org.jetbrains.skija.WStream
import org.jetbrains.skija.svg.SVGCanvas
import org.jetbrains.skija.svg.SVGLength
import org.jetbrains.skija.svg.SVGLengthType
import org.jetbrains.skija.svg.SVGLengthUnit
import org.jetbrains.skija.svg.SVGLengthContext
import org.jetbrains.skija.svg.SVGPreserveAspectRatio
import org.jetbrains.skija.svg.SVGPreserveAspectRatioAlign
import org.jetbrains.skija.svg.SVGPreserveAspectRatioScale
import org.jetbrains.skija.ColorAlphaType
import org.jetbrains.skija.AnimationDisposalMode
import org.jetbrains.skija.BlendMode
import org.jetbrains.skija.IRect
import org.jetbrains.skija.AnimationFrameInfo
import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.IHasImageInfo
import org.jetbrains.skija.ImageInfo
import org.jetbrains.skija.IPoint
import org.jetbrains.skija.PixelRef
import org.jetbrains.skija.Shader
import org.jetbrains.skija.FilterTileMode
import org.jetbrains.skija.SamplingMode
import org.jetbrains.skija.U16String
import org.jetbrains.skija.SurfaceProps
import org.jetbrains.skija.RRect
import org.jetbrains.skija.ClipMode
import org.jetbrains.skija.FilterMode
import org.jetbrains.skija.Picture
import org.jetbrains.skija.Matrix44
import org.jetbrains.skija.EncodedOrigin
import org.jetbrains.skija.EncodedImageFormat
import org.jetbrains.skija.Color4f
import org.jetbrains.skija.ColorChannel
import org.jetbrains.skija.ColorFilter
import org.jetbrains.skija.ColorMatrix
import org.jetbrains.skija.ColorFilter._LinearToSRGBGammaHolder
import org.jetbrains.skija.ColorFilter._SRGBToLinearGammaHolder
import org.jetbrains.skija.InversionMode
import org.jetbrains.skija.ColorFilter._LumaHolder
import org.jetbrains.skija.ColorInfo
import org.jetbrains.skija.ColorSpace._SRGBHolder
import org.jetbrains.skija.ColorSpace._SRGBLinearHolder
import org.jetbrains.skija.ColorSpace._DisplayP3Holder
import org.jetbrains.skija.ContentChangeMode
import org.jetbrains.skija.CubicResampler
import org.jetbrains.skija.DirectContext
import org.jetbrains.skija.GLBackendState
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.skija.FilterBlurMode
import org.jetbrains.skija.MipmapMode
import org.jetbrains.skija.FilterMipmap
import org.jetbrains.skija.FilterQuality
import org.jetbrains.skija.FontEdging
import org.jetbrains.skija.FontHinting
import org.jetbrains.skija.FontExtents
import org.jetbrains.skija.FontFamilyName
import org.jetbrains.skija.FontMgr._DefaultHolder
import org.jetbrains.skija.FontStyleSet
import org.jetbrains.skija.FontSlant
import org.jetbrains.skija.FontWidth
import org.jetbrains.skija.FontVariation
import org.jetbrains.skija.FontVariationAxis
import org.jetbrains.skija.GradientStyle
import org.jetbrains.skija.MaskFilter
import org.jetbrains.skija.OutputWStream
import org.jetbrains.skija.PaintMode
import org.jetbrains.skija.PaintStrokeCap
import org.jetbrains.skija.PaintStrokeJoin
import org.jetbrains.skija.PathEffect
import org.jetbrains.skija.PaintFilterCanvas
import org.jetbrains.skija.PathSegment
import org.jetbrains.skija.PathOp
import org.jetbrains.skija.PathFillMode
import org.jetbrains.skija.PathVerb
import org.jetbrains.skija.PathEllipseArc
import org.jetbrains.skija.PathDirection
import org.jetbrains.skija.PathSegmentIterator
import org.jetbrains.skija.RSXform
import org.jetbrains.skija.PathMeasure
import org.jetbrains.skija.PictureRecorder
import org.jetbrains.skija.PixelGeometry
import org.jetbrains.skija.Point3
import org.jetbrains.skija.RuntimeEffect
import org.jetbrains.skija.ShadowUtils
import org.jetbrains.skija.SurfaceOrigin
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.TextBlobBuilder
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference
import java.util.*
import java.util.function.IntFunction

class Typeface @ApiStatus.Internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        /**
         * @return  the default normal typeface, which is never null
         */
        fun makeDefault(): Typeface {
            Stats.onNativeCall()
            return Typeface(_nMakeDefault())
        }

        /**
         * Creates a new reference to the typeface that most closely matches the
         * requested name and style. This method allows extended font
         * face specifiers as in the [FontStyle] type. Will never return null.
         * @param name   May be null. The name of the font family
         * @param style  The style of the typeface
         * @return       reference to the closest-matching typeface
         */
        fun makeFromName(name: String?, style: FontStyle): Typeface {
            Stats.onNativeCall()
            return Typeface(_nMakeFromName(name, style._value))
        }
        /**
         * @return  a new typeface given a file
         * @throws IllegalArgumentException  If the file does not exist, or is not a valid font file
         */
        /**
         * @return  a new typeface given a file
         * @throws IllegalArgumentException  If the file does not exist, or is not a valid font file
         */
        @JvmOverloads
        fun makeFromFile(path: String, index: Int = 0): Typeface {
            Stats.onNativeCall()
            val ptr = _nMakeFromFile(path, index)
            require(ptr != 0L) { "Failed to create Typeface from path=\"$path\" index=$index" }
            return Typeface(ptr)
        }
        /**
         * @return  a new typeface given a Data
         * @throws IllegalArgumentException  If the data is null, or is not a valid font file
         */
        /**
         * @return  a new typeface given a Data
         * @throws IllegalArgumentException  If the data is null, or is not a valid font file
         */
        @JvmOverloads
        fun makeFromData(data: Data, index: Int = 0): Typeface {
            return try {
                Stats.onNativeCall()
                val ptr = _nMakeFromData(Native.Companion.getPtr(data), index)
                require(ptr != 0L) { "Failed to create Typeface from data $data" }
                Typeface(ptr)
            } finally {
                Reference.reachabilityFence(data)
            }
        }

        @ApiStatus.Internal
        external fun _nGetFontStyle(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nIsFixedPitch(ptr: Long): Boolean
        @ApiStatus.Internal
        external fun _nGetVariations(ptr: Long): Array<FontVariation>?
        @ApiStatus.Internal
        external fun _nGetVariationAxes(ptr: Long): Array<FontVariationAxis>?
        @ApiStatus.Internal
        external fun _nGetUniqueId(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nEquals(ptr: Long, otherPtr: Long): Boolean
        @ApiStatus.Internal
        external fun _nMakeDefault(): Long
        @ApiStatus.Internal
        external fun _nMakeFromName(name: String?, fontStyle: Int): Long
        @ApiStatus.Internal
        external fun _nMakeFromFile(path: String?, index: Int): Long
        @ApiStatus.Internal
        external fun _nMakeFromData(dataPtr: Long, index: Int): Long
        @ApiStatus.Internal
        external fun _nMakeClone(ptr: Long, variations: Array<FontVariation>?, collectionIndex: Int): Long
        @ApiStatus.Internal
        external fun _nGetUTF32Glyphs(ptr: Long, uni: IntArray?): ShortArray
        @ApiStatus.Internal
        external fun _nGetUTF32Glyph(ptr: Long, unichar: Int): Short
        @ApiStatus.Internal
        external fun _nGetGlyphsCount(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nGetTablesCount(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nGetTableTags(ptr: Long): IntArray?
        @ApiStatus.Internal
        external fun _nGetTableSize(ptr: Long, tag: Int): Long
        @ApiStatus.Internal
        external fun _nGetTableData(ptr: Long, tag: Int): Long
        @ApiStatus.Internal
        external fun _nGetUnitsPerEm(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nGetKerningPairAdjustments(ptr: Long, glyphs: ShortArray?): IntArray?
        @ApiStatus.Internal
        external fun _nGetFamilyNames(ptr: Long): Array<FontFamilyName>
        @ApiStatus.Internal
        external fun _nGetFamilyName(ptr: Long): String
        @ApiStatus.Internal
        external fun _nGetBounds(ptr: Long): Rect

        init {
            staticLoad()
        }
    }

    /**
     * @return  the typeface’s intrinsic style attributes
     */
    val fontStyle: FontStyle
        get() = try {
            Stats.onNativeCall()
            org.jetbrains.skija.FontStyle(_nGetFontStyle(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * @return  true if [.getFontStyle] has the bold bit set
     */
    val isBold: Boolean
        get() = fontStyle.weight >= FontWeight.Companion.SEMI_BOLD

    /**
     * @return  true if [.getFontStyle] has the italic bit set
     */
    val isItalic: Boolean
        get() = fontStyle.slant != FontSlant.UPRIGHT

    /**
     * This is a style bit, advance widths may vary even if this returns true.
     * @return  true if the typeface claims to be fixed-pitch
     */
    val isFixedPitch: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsFixedPitch(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * It is possible the number of axes can be retrieved but actual position cannot.
     * @return  the variation coordinates describing the position of this typeface in design variation space, null if there’s no variations
     */
    val variations: Array<FontVariation>?
        get() = try {
            Stats.onNativeCall()
            _nGetVariations(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * It is possible the number of axes can be retrieved but actual position cannot.
     * @return  the variation coordinates describing the position of this typeface in design variation space, null if there’s no variations
     */
    val variationAxes: Array<FontVariationAxis>?
        get() = try {
            Stats.onNativeCall()
            _nGetVariationAxes(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * @return  a 32bit value for this typeface, unique for the underlying font data. Never 0
     */
    val uniqueId: Int
        get() = try {
            Stats.onNativeCall()
            _nGetUniqueId(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * @return  true if the two typefaces reference the same underlying font, treating null as the default font
     */
    @ApiStatus.Internal
    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            _nEquals(_ptr, Native.Companion.getPtr(other))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(other)
        }
    }

    /**
     * Return a new typeface based on this typeface but parameterized as specified in the
     * variation. If the variation does not supply an argument for a parameter
     * in the font then the value from this typeface will be used as the value for that argument.
     * @return  same typeface if variation already matches, new typeface otherwise
     * @throws IllegalArgumentException  on failure
     */
    fun makeClone(variation: FontVariation): Typeface {
        return makeClone(arrayOf(variation), 0)
    }
    /**
     * Return a new typeface based on this typeface but parameterized as specified in the
     * variations. If the variations does not supply an argument for a parameter
     * in the font then the value from this typeface will be used as the value for that argument.
     * @return  same typeface if all variation already match, new typeface otherwise
     * @throws IllegalArgumentException  on failure
     */
    /**
     * Return a new typeface based on this typeface but parameterized as specified in the
     * variations. If the variations does not supply an argument for a parameter
     * in the font then the value from this typeface will be used as the value for that argument.
     * @return  same typeface if all variation already match, new typeface otherwise
     * @throws IllegalArgumentException  on failure
     */
    @JvmOverloads
    fun makeClone(variations: Array<FontVariation>, collectionIndex: Int = 0): Typeface {
        return try {
            if (variations.size == 0) return this
            Stats.onNativeCall()
            val ptr = _nMakeClone(_ptr, variations, collectionIndex)
            require(ptr != 0L) {
                "Failed to clone Typeface $this with " + Arrays.toString(
                    variations
                )
            }
            Typeface(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     * Given a string, returns corresponding glyph ids.
     *
     * @return  the corresponding glyph ids for each character.
     */
    fun getStringGlyphs(s: String): ShortArray {
        return getUTF32Glyphs(s.codePoints().toArray())
    }

    /**
     * Given an array of UTF32 character codes, return their corresponding glyph IDs.
     *
     * @return  the corresponding glyph IDs for each character.
     */
    fun getUTF32Glyphs(uni: IntArray?): ShortArray {
        return try {
            Stats.onNativeCall()
            _nGetUTF32Glyphs(_ptr, uni)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     * This is a short-cut for calling [.getUTF32Glyphs].
     * @return  the glyph that corresponds to the specified unicode code-point (in UTF32 encoding). If the unichar is not supported, returns 0
     */
    fun getUTF32Glyph(unichar: Int): Short {
        return try {
            Stats.onNativeCall()
            _nGetUTF32Glyph(_ptr, unichar)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     * @return  the number of glyphs in the typeface
     */
    val glyphsCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetGlyphsCount(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * @return  the number of tables in the font
     */
    val tablesCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetTablesCount(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * @return  the list of table tags in the font
     */
    val tableTags: Array<String>
        get() = try {
            Stats.onNativeCall()
            Arrays.stream(_nGetTableTags(_ptr))
                .mapToObj(IntFunction<String> { tag: Int -> FourByteTag.Companion.toString(tag) })
                .toArray { _Dummy_.__Array__() }
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * Given a table tag, return the size of its contents, or 0 if not present
     */
    fun getTableSize(tag: String): Long {
        return try {
            Stats.onNativeCall()
            _nGetTableSize(_ptr, FourByteTag.Companion.fromString(tag))
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     * Return an immutable copy of the requested font table, or null if that table was
     * not found.
     *
     * @param tag  The table tag whose contents are to be copied
     * @return     an immutable copy of the table's data, or null
     */
    fun getTableData(tag: String): Data? {
        return try {
            Stats.onNativeCall()
            val ptr = _nGetTableData(_ptr, FourByteTag.Companion.fromString(tag))
            if (ptr == 0L) null else org.jetbrains.skija.Data(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     * @return  the units-per-em value for this typeface, or zero if there is an error
     */
    val unitsPerEm: Int
        get() = try {
            Stats.onNativeCall()
            _nGetUnitsPerEm(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * Given a run of glyphs, return the associated horizontal adjustments.
     * Adjustments are in "design units", which are integers relative to the
     * typeface's units per em (see [.getUnitsPerEm]).
     *
     * Some typefaces are known to never support kerning. Calling this with null,
     * if it returns null then it will always return null (no kerning) for all
     * possible glyph runs. If it returns int[0], then it *may* return non-null
     * adjustments for some glyph runs.
     *
     * @return  adjustment array (one less than glyphs), or null if no kerning should be applied
     */
    fun getKerningPairAdjustments(glyphs: ShortArray?): IntArray? {
        return try {
            Stats.onNativeCall()
            _nGetKerningPairAdjustments(_ptr, glyphs)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     * @return  all of the family names specified by the font
     */
    val familyNames: Array<FontFamilyName>
        get() = try {
            Stats.onNativeCall()
            _nGetFamilyNames(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * @return  the family name for this typeface. The language of the name is whatever the host platform chooses
     */
    val familyName: String
        get() = try {
            Stats.onNativeCall()
            _nGetFamilyName(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * Return a rectangle (scaled to 1-pt) that represents the union of the bounds of all
     * of the glyphs, but each one positioned at (0,). This may be conservatively large, and
     * will not take into account any hinting or other size-specific adjustments.
     */
    val bounds: Rect
        get() = try {
            Stats.onNativeCall()
            _nGetBounds(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
}