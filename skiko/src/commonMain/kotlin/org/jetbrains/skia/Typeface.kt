package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

class Typeface internal constructor(ptr: Long) : RefCnt(ptr) {
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
        fun makeFromData(data: Data, index: Int = 0): Typeface {
            return try {
                Stats.onNativeCall()
                val ptr = _nMakeFromData(getPtr(data), index)
                require(ptr != 0L) { "Failed to create Typeface from data $data" }
                Typeface(ptr)
            } finally {
                reachabilityBarrier(data)
            }
        }

        @JvmStatic external fun _nGetFontStyle(ptr: Long): Int
        @JvmStatic external fun _nIsFixedPitch(ptr: Long): Boolean
        @JvmStatic external fun _nGetVariations(ptr: Long): Array<FontVariation>?
        @JvmStatic external fun _nGetVariationAxes(ptr: Long): Array<FontVariationAxis>?
        @JvmStatic external fun _nGetUniqueId(ptr: Long): Int
        @JvmStatic external fun _nEquals(ptr: Long, otherPtr: Long): Boolean
        @JvmStatic external fun _nMakeDefault(): Long
        @JvmStatic external fun _nMakeFromName(name: String?, fontStyle: Int): Long
        @JvmStatic external fun _nMakeFromFile(path: String?, index: Int): Long
        @JvmStatic external fun _nMakeFromData(dataPtr: Long, index: Int): Long
        @JvmStatic external fun _nMakeClone(ptr: Long, variations: Array<FontVariation>?, collectionIndex: Int): Long
        @JvmStatic external fun _nGetUTF32Glyphs(ptr: Long, uni: IntArray?): ShortArray
        @JvmStatic external fun _nGetUTF32Glyph(ptr: Long, unichar: Int): Short
        @JvmStatic external fun _nGetGlyphsCount(ptr: Long): Int
        @JvmStatic external fun _nGetTablesCount(ptr: Long): Int
        @JvmStatic external fun _nGetTableTags(ptr: Long): IntArray?
        @JvmStatic external fun _nGetTableSize(ptr: Long, tag: Int): Long
        @JvmStatic external fun _nGetTableData(ptr: Long, tag: Int): Long
        @JvmStatic external fun _nGetUnitsPerEm(ptr: Long): Int
        @JvmStatic external fun _nGetKerningPairAdjustments(ptr: Long, glyphs: ShortArray?): IntArray?
        @JvmStatic external fun _nGetFamilyNames(ptr: Long): Array<FontFamilyName>
        @JvmStatic external fun _nGetFamilyName(ptr: Long): String
        @JvmStatic external fun _nGetBounds(ptr: Long): Rect

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
            _nGetVariationAxes(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  a 32bit value for this typeface, unique for the underlying font data. Never 0
     */
    val uniqueId: Int
        get() = try {
            Stats.onNativeCall()
            _nGetUniqueId(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  true if the two typefaces reference the same underlying font, treating null as the default font
     */
    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            _nEquals(_ptr, getPtr(other))
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
            require(ptr != 0L) {
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
            _nGetUTF32Glyphs(_ptr, uni)
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
            _nGetUTF32Glyph(_ptr, unichar)
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
    fun getTableSize(tag: String): Long {
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
            if (ptr == 0L) null else Data(ptr)
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
            _nGetFamilyName(_ptr)
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
            _nGetBounds(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
}