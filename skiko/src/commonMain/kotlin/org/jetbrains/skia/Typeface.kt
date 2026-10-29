package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skiko.internal.unpackTo
import kotlin.jvm.JvmStatic

class Typeface internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }

        @JvmStatic
        fun makeEmpty(): Typeface = nativeCallFromStatic { Typeface(_nMakeEmptyTypeface()) }
    }

    /**
     * @return  the typeface’s intrinsic style attributes
     */
    val fontStyle: FontStyle
        get() = nativeCall { FontStyle(_nGetFontStyle(_ptr)) }

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
        get() = nativeCall { _nIsFixedPitch(_ptr) }

    /**
     * It is possible the number of axes can be retrieved but actual position cannot.
     * @return  the variation coordinates describing the position of this typeface in design variation space, null if there’s no variations
     */
    val variations: Array<FontVariation>?
        get() = nativeCall {
            val count = _nGetVariationsCount(_ptr)
            if (count > 0) {
                val variationsData = withResult(IntArray(count * 2)) {
                    _nGetVariations(_ptr, it, count)
                }
                (0 until count).map { i ->
                    val j = 2 * i
                    FontVariation(FourByteTag(variationsData[j]), Float.fromBits(variationsData[j + 1]))
                }.toTypedArray()
            } else null
        }

    /**
     * It is possible the number of axes can be retrieved but actual position cannot.
     * @return  the variation coordinates describing the position of this typeface in design variation space, null if there’s no variations
     */
    val variationAxes: Array<FontVariationAxis>?
        get() = nativeCall {
            val axisCount = _nGetVariationAxesCount(_ptr)
            if (axisCount <= 0) {
                null
            } else {
                val axisData = withResult(IntArray(axisCount * 5)) {
                    _nGetVariationAxes(_ptr, it, axisCount)
                }
                (0 until axisCount).map { i ->
                    val j = 5 * i
                    FontVariationAxis(
                        FourByteTag(axisData[j]),
                        Float.fromBits(axisData[j + 1]),
                        Float.fromBits(axisData[j + 2]),
                        Float.fromBits(axisData[j + 3]),
                        axisData[j + 4] != 0
                    )
                }.toTypedArray()
            }
        }

    /**
     * @return  a 32bit value for this typeface, unique for the underlying font data. Never 0
     */
    val uniqueId: Int
        get() = nativeCall { Typeface_nGetUniqueId(_ptr) }

    /**
     * @return  true if the two typefaces reference the same underlying font, treating null as the default font
     */
    override fun nativeEquals(other: Native?): Boolean = nativeCall(other) { other ->
        Typeface_nEquals(_ptr, other.pointerOrNullPointer)
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
        if (variations.isEmpty()) return this
        return nativeCall {
            val variationsCount = variations.size * 2
            val variationsData = variations.unpackTo(IntArray(variationsCount)) { variation, arr, idx ->
                arr[idx] = variation._tag.tag
                arr[idx + 1] = variation.value.toRawBits()
                //Stepping 2 places for the next variation
                idx + 2
            }
            val ptr = interopScope { _nMakeClone(_ptr, toInterop(variationsData), variationsCount, collectionIndex) }
            require(ptr != NullPointer) {
                "Failed to clone Typeface $this with ${variations.contentToString()}"
            }
            Typeface(ptr)
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
    fun getUTF32Glyphs(uni: IntArray?): ShortArray = nativeCall {
        if (uni != null) {
            interopScope {
                withResult(ShortArray(uni.size)) {
                    Typeface_nGetUTF32Glyphs(_ptr, toInterop(uni), uni.size, it)
                }
            }
        } else shortArrayOf()
    }

    /**
     * This is a short-cut for calling [.getUTF32Glyphs].
     * @return  the glyph that corresponds to the specified unicode code-point (in UTF32 encoding). If the unichar is not supported, returns 0
     */
    fun getUTF32Glyph(unichar: Int): Short = nativeCall { Typeface_nGetUTF32Glyph(_ptr, unichar) }

    /**
     * @return  the number of glyphs in the typeface
     */
    val glyphsCount: Int = nativeCall { _nGetGlyphsCount(_ptr) }

    /**
     * @return  the number of tables in the font
     */
    val tablesCount: Int = nativeCall { _nGetTablesCount(_ptr) }

    /**
     * @return  the list of table tags in the font
     */
    val tableTags: Array<String>
        get() = nativeCall {
            val count = _nGetTableTagsCount(_ptr)
            if (count > 0) {
                val tags = withResult(IntArray(count)) {
                    _nGetTableTags(_ptr, it, count)
                }
                Array(count) {
                    FourByteTag.toString(FourByteTag(tags[it]))
                }
            } else emptyArray()
        }

    /**
     * Given a table tag, return the size of its contents, or 0 if not present
     */
    fun getTableSize(tag: String): NativePointer = nativeCall { _nGetTableSize(_ptr, FourByteTag.fromString(tag).tag) }

    /**
     * Return an immutable copy of the requested font table, or null if that table was
     * not found.
     *
     * @param tag  The table tag whose contents are to be copied
     * @return     an immutable copy of the table's data, or null
     */
    fun getTableData(tag: String): Data? = nativeCall {
        val ptr = _nGetTableData(_ptr, FourByteTag.fromString(tag).tag)
        if (ptr == NullPointer) null else Data(ptr)
    }

    /**
     * @return  the units-per-em value for this typeface, or zero if there is an error
     */
    val unitsPerEm: Int = nativeCall { _nGetUnitsPerEm(_ptr) }

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
    fun getKerningPairAdjustments(glyphs: ShortArray?): IntArray? = nativeCall {
        if (glyphs != null) {
            if (glyphs.isNotEmpty()) {
                withNullableResult(IntArray(glyphs.size)) {
                    _nGetKerningPairAdjustments(_ptr, toInterop(glyphs), glyphs.size, it)
                }
            } else null
        } else null
    }

    /**
     * @return  all of the family names specified by the font
     */
    val familyNames: Array<FontFamilyName>
        get() = nativeCall {
            arrayDecoderScope({
                ArrayDecoder(_nGetFamilyNames(_ptr), ManagedString_nGetFinalizer())
            }) { arrayDecoder ->
                val size = arrayDecoder.size
                (0 until size / 2).map { i ->
                    val name = withStringResult(arrayDecoder.release(2 * i))
                    val language = withStringResult(arrayDecoder.release(2 * i + 1))
                    FontFamilyName(name, language)
                }.toTypedArray()
            }
        }

    /**
     * @return  the family name for this typeface. The language of the name is whatever the host platform chooses
     */
    val familyName: String = nativeCall {
        withStringResult {
            _nGetFamilyName(_ptr)
        }
    }

    /**
     * Return a rectangle (scaled to 1-pt) that represents the union of the bounds of all
     * of the glyphs, but each one positioned at (0,). This may be conservatively large, and
     * will not take into account any hinting or other size-specific adjustments.
     */
    val bounds: Rect = nativeCall { Rect.fromInteropPointer { Typeface_nGetBounds(_ptr, it) } }

    override fun toString() = "Typeface(familyName='$familyName', fontStyle=$fontStyle, uniqueId=$uniqueId)"
}

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetUniqueId")
// No ForceNativeThreadState: returns the stored unique id, so it is short and cannot block.
// Native call path: SkTypeface::uniqueID
private external fun Typeface_nGetUniqueId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nEquals")
// No ForceNativeThreadState: compares two typeface pointers and their stored unique ids, so it is short and cannot
// block.
// Native call path: SkTypeface::Equal
private external fun Typeface_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetUTF32Glyphs")
// Needs ForceNativeThreadState: maps each character to a glyph through CoreText, looping over every character, so its
// time grows with the input length.
// Native call path: SkTypeface::unicharsToGlyphs -> SkTypeface_Mac::onCharsToGlyphs -> CTFontGetGlyphsForCharacters
@ForceNativeThreadState
private external fun Typeface_nGetUTF32Glyphs(
    ptr: NativePointer,
    uni: InteropPointer,
    count: Int,
    glyphs: InteropPointer
)

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetUTF32Glyph")
// Needs ForceNativeThreadState: maps one character to a glyph through CoreText, so it can call CoreText.
// Native call path: SkTypeface::unicharToGlyph -> SkTypeface_Mac::onCharsToGlyphs -> CTFontGetGlyphsForCharacters
@ForceNativeThreadState
private external fun Typeface_nGetUTF32Glyph(ptr: NativePointer, unichar: Int): Short

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetBounds")
// Needs ForceNativeThreadState: may make a CoreText font copy and read its bounding box, so it can call CoreText.
// Native call path: SkTypeface::getBounds -> onComputeBounds -> SkScalerContext_Mac::generateFontMetrics
@ForceNativeThreadState
private external fun Typeface_nGetBounds(ptr: NativePointer, bounds: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetFontStyle")
// No ForceNativeThreadState: returns a stored style value, so it is short and cannot block.
// Native call path: SkTypeface::fontStyle -> onGetFontStyle
private external fun _nGetFontStyle(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nIsFixedPitch")
// No ForceNativeThreadState: returns a stored fixed-width flag, so it is short and cannot block.
// Native call path: SkTypeface::isFixedPitch -> onGetFixedPitch
private external fun _nIsFixedPitch(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetVariationsCount")
// Needs ForceNativeThreadState: reads font variation settings through CoreText and walks CoreFoundation arrays, so it
// can call CoreText.
// Native call path: getVariationDesignPosition -> SkTypeface_Mac::onGetVariationDesignPosition -> CTFontCopyVariation
@ForceNativeThreadState
private external fun _nGetVariationsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetVariations")
// Needs ForceNativeThreadState: reads each variation axis value through CoreText and CoreFoundation, so it can call
// CoreText.
// Native call path: getVariationDesignPosition -> SkTypeface_Mac::onGetVariationDesignPosition -> CTFontCopyVariation
@ForceNativeThreadState
private external fun _nGetVariations(ptr: NativePointer, variations: InteropPointer, count: Int)

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetVariationAxesCount")
// Needs ForceNativeThreadState: reads the font's variation axes through CoreText, so it can call CoreText.
// Native call path: getVariationDesignParameters -> SkTypeface_Mac::onGetVariationDesignParameters
@ForceNativeThreadState
private external fun _nGetVariationAxesCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetVariationAxes")
// Needs ForceNativeThreadState: reads each variation axis through CoreText and looks up values in CoreFoundation
// dictionaries, so it can call CoreText.
// Native call path: getVariationDesignParameters -> SkTypeface_Mac::onGetVariationDesignParameters
@ForceNativeThreadState
private external fun _nGetVariationAxes(ptr: NativePointer, axisData: InteropPointer, axisCount: Int)

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nMakeClone")
// Needs ForceNativeThreadState: makes a copy of the font through CoreText, so it can call CoreText.
// Native call path: SkTypeface::makeClone -> SkTypeface_Mac::onMakeClone -> CTFontCreateCopyWithAttributes
@ForceNativeThreadState
private external fun _nMakeClone(
    ptr: NativePointer,
    variations: InteropPointer,
    variationsCount: Int,
    collectionIndex: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nMakeEmptyTypeface")
// Needs ForceNativeThreadState: creates a shared empty typeface on first use, which can wait for another thread that is
// also creating it.
// Native call path: SkTypeface::MakeEmpty -> SkEmptyTypeface::Make
@ForceNativeThreadState
private external fun _nMakeEmptyTypeface(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetGlyphsCount")
// Needs ForceNativeThreadState: asks CoreText how many glyphs the font has, so it can call CoreText.
// Native call path: SkTypeface::countGlyphs -> SkTypeface_Mac::onCountGlyphs -> CTFontGetGlyphCount
@ForceNativeThreadState
private external fun _nGetGlyphsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTablesCount")
// Needs ForceNativeThreadState: asks CoreText for the font's table list, so it can call CoreText.
// Native call path: SkTypeface::countTables -> SkTypeface_Mac::onGetTableTags -> CTFontCopyAvailableTables
@ForceNativeThreadState
private external fun _nGetTablesCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTableTagsCount")
// Needs ForceNativeThreadState: asks CoreText for the font's table list, so it can call CoreText.
// Native call path: SkTypeface::countTables -> SkTypeface_Mac::onGetTableTags -> CTFontCopyAvailableTables
@ForceNativeThreadState
private external fun _nGetTableTagsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTableTags")
// Needs ForceNativeThreadState: asks CoreText for the font's table list and loops over the tags, so it can call
// CoreText.
// Native call path: SkTypeface::readTableTags -> SkTypeface_Mac::onGetTableTags -> CTFontCopyAvailableTables
@ForceNativeThreadState
private external fun _nGetTableTags(ptr: NativePointer, tags: InteropPointer, count: Int)

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTableSize")
// Needs ForceNativeThreadState: copies a font table through CoreText, or through CoreGraphics as a fallback, so it can
// call both.
// Native call path: SkTypeface::getTableSize -> SkTypeface_Mac::onGetTableData -> CTFontCopyTable
@ForceNativeThreadState
private external fun _nGetTableSize(ptr: NativePointer, tag: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetTableData")
// Needs ForceNativeThreadState: copies a font table through CoreText, so it can call CoreText.
// Native call path: SkTypeface::copyTableData -> SkTypeface_Mac::onCopyTableData -> CTFontCopyTable
@ForceNativeThreadState
private external fun _nGetTableData(ptr: NativePointer, tag: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetUnitsPerEm")
// Needs ForceNativeThreadState: asks CoreText for the font's units-per-em size, so it can call CoreText.
// Native call path: SkTypeface::getUnitsPerEm -> SkTypeface_Mac::onGetUPEM -> CTFontGetUnitsPerEm
@ForceNativeThreadState
private external fun _nGetUnitsPerEm(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetKerningPairAdjustments")
// Needs ForceNativeThreadState: looks up kerning for every pair under the FreeType lock, so its time grows with the
// pair count.
// Native call path: SkTypeface_FreeType::onGetKerningPairAdjustments -> FT_Get_Kerning
@ForceNativeThreadState
private external fun _nGetKerningPairAdjustments(
    ptr: NativePointer,
    glyphs: InteropPointer,
    count: Int,
    adjustments: InteropPointer
): Boolean

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetFamilyNames")
// Needs ForceNativeThreadState: reads the font's name table through CoreText and loops over the names, so it can call
// CoreText.
// Native call path: SkTypeface_Mac::onCreateFamilyNameIterator -> MakeForFamilyNames -> CTFontCopyTable
@ForceNativeThreadState
private external fun _nGetFamilyNames(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Typeface__1nGetFamilyName")
// Needs ForceNativeThreadState: asks CoreText for the family name and converts the CoreFoundation string, so it can
// call CoreText.
// Native call path: SkTypeface::getFamilyName -> SkTypeface_Mac::onGetFamilyName -> CTFontCopyFamilyName
@ForceNativeThreadState
private external fun _nGetFamilyName(ptr: NativePointer): NativePointer
// </editor-fold>
