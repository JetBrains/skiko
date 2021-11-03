package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skiko.kotlinBackend

class Typeface internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        /**
         * @return  the default normal typeface, which is never null
         */
        fun makeDefault(): Typeface {
            Stats.onNativeCall()
            return Typeface(Typeface_nMakeDefault())
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
         * @return  a new typeface given a Data
         * @throws IllegalArgumentException  If the data is null, or is not a valid font file
         */
        fun makeFromData(data: Data, index: Int = 0): Typeface {
            return try {
                Stats.onNativeCall()
                val ptr = _nMakeFromData(getPtr(data), index)
                require(ptr != NullPointer) { "Failed to create Typeface from data $data" }
                Typeface(ptr)
            } finally {
                reachabilityBarrier(data)
            }
        }

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
            org.jetbrains.skia.FontStyle(_nGetFontStyle(_ptr))
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
            _nIsFixedPitch(_ptr)
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
            _nGetVariations(_ptr)
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
            val axisCount = _nGetVariationAxesCount(_ptr)
            if (axisCount == 0) {
                null
            } else {
                val axisData = withResult(IntArray(axisCount * 5)) {
                    _nGetVariationAxes(_ptr, it, axisCount)
                }
                (0 until axisCount).map { i ->
                    val j = 5 * i
                    FontVariationAxis(axisData[j], Float.fromBits(axisData[j + 1]), Float.fromBits(axisData[j + 2]), Float.fromBits(axisData[j + 3]), axisData[j + 4] != 0)
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
    override fun _nativeEquals(other: Native?): Boolean {
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
            val ptr = _nMakeClone(_ptr, variations, collectionIndex)
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
        return getUTF32Glyphs(s.intCodePoints())
    }

    /**
     * Given an array of UTF32 character codes, return their corresponding glyph IDs.
     *
     * @return  the corresponding glyph IDs for each character.
     */
    fun getUTF32Glyphs(uni: IntArray?): ShortArray {
        return try {
            Stats.onNativeCall()
            Typeface_nGetUTF32Glyphs(_ptr, uni)
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
            _nGetGlyphsCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  the number of tables in the font
     */
    val tablesCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetTablesCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  the list of table tags in the font
     */
    val tableTags: Array<String>
        get() = try {
            Stats.onNativeCall()
            _nGetTableTags(_ptr)!!.map { tag -> FourByteTag.toString(tag) }.toTypedArray()
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Given a table tag, return the size of its contents, or 0 if not present
     */
    fun getTableSize(tag: String): NativePointer {
        return try {
            Stats.onNativeCall()
            _nGetTableSize(_ptr, FourByteTag.fromString(tag))
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
            val ptr = _nGetTableData(_ptr, FourByteTag.fromString(tag))
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
            _nGetUnitsPerEm(_ptr)
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
            _nGetKerningPairAdjustments(_ptr, glyphs)
        } finally {
            reachabilityBarrier(this)
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
            reachabilityBarrier(this)
        }

    /**
     * @return  the family name for this typeface. The language of the name is whatever the host platform chooses
     */
    val familyName: String
        get() = try {
            Stats.onNativeCall()
            withStringResult {
                _nGetFamilyName(_ptr)
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
            Typeface_nGetBounds(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
}

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetUniqueId")
private external fun Typeface_nGetUniqueId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nEquals")
private external fun Typeface_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nMakeDefault")
private external fun Typeface_nMakeDefault(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetUTF32Glyphs")
private external fun Typeface_nGetUTF32Glyphs(ptr: NativePointer, uni: IntArray?): ShortArray

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetUTF32Glyph")
private external fun Typeface_nGetUTF32Glyph(ptr: NativePointer, unichar: Int): Short

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetBounds")
private external fun Typeface_nGetBounds(ptr: NativePointer): Rect

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetFontStyle")
private external fun _nGetFontStyle(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nIsFixedPitch")
private external fun _nIsFixedPitch(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetVariations")
private external fun _nGetVariations(ptr: NativePointer): Array<FontVariation>?

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetVariationAxesCount")
private external fun _nGetVariationAxesCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetVariationAxes")
private external fun _nGetVariationAxes(ptr: NativePointer, axisData: InteropPointer, axisCount: Int)

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nMakeFromName")
private external fun _nMakeFromName(name: String?, fontStyle: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nMakeFromFile")
internal external fun _nMakeFromFile(path: InteropPointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nMakeFromData")
private external fun _nMakeFromData(dataPtr: NativePointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nMakeClone")
private external fun _nMakeClone(ptr: NativePointer, variations: Array<FontVariation>?, collectionIndex: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetGlyphsCount")
private external fun _nGetGlyphsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTablesCount")
private external fun _nGetTablesCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTableTags")
private external fun _nGetTableTags(ptr: NativePointer): IntArray?

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTableSize")
private external fun _nGetTableSize(ptr: NativePointer, tag: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTableData")
private external fun _nGetTableData(ptr: NativePointer, tag: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetUnitsPerEm")
private external fun _nGetUnitsPerEm(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetKerningPairAdjustments")
private external fun _nGetKerningPairAdjustments(ptr: NativePointer, glyphs: ShortArray?): IntArray?

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetFamilyNames")
private external fun _nGetFamilyNames(ptr: NativePointer): Array<FontFamilyName>

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetFamilyName")
private external fun _nGetFamilyName(ptr: NativePointer): NativePointer