package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withResult
import kotlin.jvm.JvmInline

@JvmInline
value class FontMetrics(internal val metrics: FloatArray) {

    //Maintained for binary compatibility
    constructor(
        top: Float,
        ascent: Float,
        descent: Float,
        bottom: Float,
        leading: Float,
        avgCharWidth: Float,
        maxCharWidth: Float,
        xMin: Float,
        xMax: Float,
        xHeight: Float,
        capHeight: Float,
        underlineThickness: Float?,
        underlinePosition: Float?,
        strikeoutThickness: Float?,
        strikeoutPosition: Float?
    ) : this(
        floatArrayOf(
            top,
            ascent,
            descent,
            bottom,
            leading,
            avgCharWidth,
            maxCharWidth,
            xMin,
            xMax,
            xHeight,
            capHeight,
            underlineThickness ?: Float.NaN,
            underlinePosition ?: Float.NaN,
            strikeoutThickness ?: Float.NaN,
            strikeoutPosition ?: Float.NaN
        )
    )

    /**
     * greatest extent above origin of any glyph bounding box, typically negative; deprecated with variable fonts
     */
    val top: Float
        get() = metrics[0]
    /**
     * distance to reserve above baseline, typically negative
     */
    val ascent: Float
         get() = metrics[1]
    /**
     * distance to reserve below baseline, typically positive
     */
    val descent: Float
        get() = metrics[2]
    /**
     * greatest extent below origin of any glyph bounding box, typically positive; deprecated with variable fonts
     */
    val bottom: Float
        get() = metrics[3]
    /**
     * distance to add between lines, typically positive or zero
     */
    val leading: Float
        get() = metrics[4]
    /**
     * average character width, zero if unknown
     */
    val avgCharWidth: Float
        get() = metrics[5]
    /**
     * maximum character width, zero if unknown
     */
    val maxCharWidth: Float
        get() = metrics[6]
    /**
     * greatest extent to left of origin of any glyph bounding box, typically negative; deprecated with variable fonts
     */
    val xMin: Float
        get() = metrics[7]
    /**
     * greatest extent to right of origin of any glyph bounding box, typically positive; deprecated with variable fonts
     */
    val xMax: Float
        get() = metrics[8]
    /**
     * height of lower-case 'x', zero if unknown, typically negative
     */
    val xHeight: Float
        get() = metrics[9]
    /**
     * height of an upper-case letter, zero if unknown, typically negative
     */
    val capHeight: Float
        get() = metrics[10]
    /**
     * underline thickness
     */
    val underlineThickness: Float?
        get() = metrics[11].asNumberOrNull()
    /**
     * distance from baseline to top of stroke, typically positive
     */
    val underlinePosition: Float?
        get() = metrics[12].asNumberOrNull()
    /**
     * strikeout thickness
     */
    val strikeoutThickness: Float?
        get() = metrics[13].asNumberOrNull()
    /**
     * distance from baseline to bottom of stroke, typically negative
     */
    val strikeoutPosition: Float?
        get() = metrics[14].asNumberOrNull()

    val height: Float
        get() = descent - ascent

    override fun toString(): String {
        return "FontMetrics(_top=$top, _ascent=$ascent, _descent=$descent, _bottom=$bottom, _leading=$leading, _avgCharWidth=$avgCharWidth, _maxCharWidth=$maxCharWidth, _xMin=$xMin, _xMax=$xMax, _xHeight=$xHeight, _capHeight=$capHeight, _underlineThickness=$underlineThickness, _underlinePosition=$underlinePosition, _strikeoutThickness=$strikeoutThickness, _strikeoutPosition=$strikeoutPosition)"
    }

    companion object
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Float.asNumberOrNull(): Float? = if (isNaN()) null else this

internal fun FontMetrics.Companion.fromInteropPointer(block: InteropScope.(InteropPointer) -> Unit) = FontMetrics(withResult(FloatArray(15), block))