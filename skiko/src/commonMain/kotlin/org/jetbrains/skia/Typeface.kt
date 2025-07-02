package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withNullableResult
import org.jetbrains.skia.impl.withResult
import org.jetbrains.skia.impl.withStringResult

class Typeface internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }

        fun makeEmpty(): Typeface {
            Stats.onNativeCall()
            return Typeface(Typeface_nMakeEmptyTypeface())
        }
    }

    /**
     * @return  the typeface’s intrinsic style attributes
     */
    val fontStyle: FontStyle
        get() = try {
            Stats.onNativeCall()
            FontStyle(Typeface_nGetFontStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  true if [.getFontStyle] has the bold bit set
     */
    val isBold: Boolean
        get() = fontStyle.weight >= FontWeight.SEMI_BOLD

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
            Typeface_nIsFixedPitch(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * It is possible the number of axes can be retrieved but actual position cannot.
     * @return  the variation coordinates describing the position of this typeface in design variation space, null if there’s no variations
     */
    val variations: Array<FontVariation>?
        get() = try {
            Stats.onNativeCall()
            val count = Typeface_nGetVariationsCount(_ptr)
            if (count > 0) {
                val variationsData = withResult(IntArray(count * 2)) {
                    Typeface_nGetVariations(_ptr, it, count)
                }
                (0 until count).map { i ->
                    val j = 2 * i
                    FontVariation(variationsData[j], Float.fromBits(variationsData[j + 1]))
                }.toTypedArray()
            } else null
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * It is possible the number of axes can be retrieved but actual position cannot.
     * @return  the variation coordinates describing the position of this typeface in design variation space, null if there’s no variations
     */
    val variationAxes: Array<FontVariationAxis>?
        get() = try {
            Stats.onNativeCall()
            val axisCount = Typeface_nGetVariationAxesCount(_ptr)
            if (axisCount <= 0) {
                null
            } else {
                val axisData = withResult(IntArray(axisCount * 5)) {
                    Typeface_nGetVariationAxes(_ptr, it, axisCount)
                }
                (0 until axisCount).map { i ->
                    val j = 5 * i
                    FontVariationAxis(
                        axisData[j],
                        Float.fromBits(axisData[j + 1]),
                        Float.fromBits(axisData[j + 2]),
                        Float.fromBits(axisData[j + 3]),
                        axisData[j + 4] != 0
                    )
                }.toTypedArray()
            }
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  a 32bit value for this typeface, unique for the underlying font data. Never 0
     */
    val uniqueId: Int
        get() = try {
            Stats.onNativeCall()
            Typeface_nGetUniqueId(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  true if the two typefaces reference the same underlying font, treating null as the default font
     */
    override fun nativeEquals(other: Native?): Boolean {
        return try {
            Typeface_nEquals(_ptr, getPtr(other))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
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
    fun makeClone(variations: Array<FontVariation>, collectionIndex: Int = 0): Typeface {
        return try {
            if (variations.size == 0) return this
            Stats.onNativeCall()
            val variationsData = variations.asList().flatMap { listOf(it._tag, it.value.toRawBits()) }.toIntArray()
            val ptr =
                interopScope { Typeface_nMakeClone(_ptr, toInterop(variationsData), 2 * variations.size, collectionIndex) }
            require(ptr != NullPointer) {
                "Failed to clone Typeface $this with $variations"
            }
            Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Given a string, returns corresponding glyph ids.
     *
     * @return  the corresponding glyph ids for each character.
     */
    fun getStringGlyphs(s: String): ShortArray {
        return getUTF32Glyphs(s.codePointsAsIntArray)
    }

    /**
     * Given an array of UTF32 character codes, return their corresponding glyph IDs.
     *
     * @return  the corresponding glyph IDs for each character.
     */
    fun getUTF32Glyphs(uni: IntArray?): ShortArray {
        return try {
            Stats.onNativeCall()
            if (uni != null) {
                interopScope {
                    withResult(ShortArray(uni.size)) {
                        Typeface_nGetUTF32Glyphs(_ptr, toInterop(uni), uni.size, it)
                    }
                }
            } else shortArrayOf()
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * This is a short-cut for calling [.getUTF32Glyphs].
     * @return  the glyph that corresponds to the specified unicode code-point (in UTF32 encoding). If the unichar is not supported, returns 0
     */
    fun getUTF32Glyph(unichar: Int): Short {
        return try {
            Stats.onNativeCall()
            Typeface_nGetUTF32Glyph(_ptr, unichar)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * @return  the number of glyphs in the typeface
     */
    val glyphsCount: Int
        get() = try {
            Stats.onNativeCall()
            Typeface_nGetGlyphsCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  the number of tables in the font
     */
    val tablesCount: Int
        get() = try {
            Stats.onNativeCall()
            Typeface_nGetTablesCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  the list of table tags in the font
     */
    val tableTags: Array<String>
        get() = try {
            Stats.onNativeCall()
            val count = Typeface_nGetTableTagsCount(_ptr)
            if (count > 0) {
                withResult(IntArray(count)) {
                    Typeface_nGetTableTags(_ptr, it, count)
                }.toList().map { FourByteTag.toString(it) }.toTypedArray()
            } else emptyArray()
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Given a table tag, return the size of its contents, or 0 if not present
     */
    fun getTableSize(tag: String): NativePointer {
        return try {
            Stats.onNativeCall()
            Typeface_nGetTableSize(_ptr, FourByteTag.fromString(tag))
        } finally {
            reachabilityBarrier(this)
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
            val ptr = Typeface_nGetTableData(_ptr, FourByteTag.fromString(tag))
            if (ptr == NullPointer) null else Data(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * @return  the units-per-em value for this typeface, or zero if there is an error
     */
    val unitsPerEm: Int
        get() = try {
            Stats.onNativeCall()
            Typeface_nGetUnitsPerEm(_ptr)
        } finally {
            reachabilityBarrier(this)
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
            if (glyphs != null) {
                if (glyphs.size > 0) {
                    withNullableResult(IntArray(glyphs.size)) {
                        Typeface_nGetKerningPairAdjustments(_ptr, toInterop(glyphs), glyphs.size, it)
                    }
                } else null
            } else null
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * @return  all of the family names specified by the font
     */
    val familyNames: Array<FontFamilyName>
        get() {
            return try {
                Stats.onNativeCall()

                arrayDecoderScope({
                    ArrayDecoder(Typeface_nGetFamilyNames(_ptr), ManagedString_nGetFinalizer())
                }) { arrayDecoder ->
                    val size = arrayDecoder.size
                    (0 until size / 2).map { i ->
                        val name = withStringResult(arrayDecoder.release(2 * i))
                        val language = withStringResult(arrayDecoder.release(2 * i + 1))
                        FontFamilyName(name, language)
                    }.toTypedArray()
                }
            } finally {
                reachabilityBarrier(this)
            }
        }

    /**
     * @return  the family name for this typeface. The language of the name is whatever the host platform chooses
     */
    val familyName: String
        get() = try {
            Stats.onNativeCall()
            withStringResult {
                Typeface_nGetFamilyName(_ptr)
            }
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Return a rectangle (scaled to 1-pt) that represents the union of the bounds of all
     * of the glyphs, but each one positioned at (0,). This may be conservatively large, and
     * will not take into account any hinting or other size-specific adjustments.
     */
    val bounds: Rect
        get() = try {
            Stats.onNativeCall()
            Rect.fromInteropPointer {
                Typeface_nGetBounds(_ptr, it)
            }
        } finally {
            reachabilityBarrier(this)
        }

    override fun toString() = "Typeface(familyName='$familyName', fontStyle=$fontStyle, uniqueId=$uniqueId)"
}