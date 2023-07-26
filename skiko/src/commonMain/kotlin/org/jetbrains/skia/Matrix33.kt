package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.withNullableResult
import org.jetbrains.skia.impl.withResult
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

internal fun Float.toRadians(): Double = this.toDouble() / 180 * PI

/**
 *
 * Matrix holds a 3x3 matrix for transforming coordinates. This allows mapping
 * Point and vectors with translation, scaling, skewing, rotation, and
 * perspective.
 *
 * 3x3 matrices are also used to characterize the gamut of a color space in the form
 * of a conversion matrix to XYZ D50. We call this a toXYZD50 matrix for short.
 *
 * @see [https://fiddle.skia.org/c/@Matrix_063](https://fiddle.skia.org/c/@Matrix_063)
 */
class Matrix33(vararg mat: Float) {
    /**
     *
     * Matrix33 elements are in row-major order.
     *
     * <pre>`
     * | scaleX   skewX  transX |
     * |  skewY  scaleY  transY |
     * | persp0  persp1  persp2 |
    `</pre> *
     */
    val mat: FloatArray
    fun makePreScale(sx: Float, sy: Float): Matrix33 {
        return Matrix33(
            mat[0] * sx,
            mat[1] * sy,
            mat[2],
            mat[3] * sx,
            mat[4] * sy,
            mat[5],
            mat[6] * sx,
            mat[7] * sy,
            mat[8]
        )
    }

    /**
     *
     * Creates Matrix33 by multiplying this by other. This can be thought of mapping by other before applying Matrix.
     *
     *
     * Given:
     *
     * <pre>`
     * | A B C |          | J K L |
     * this = | D E F |, other = | M N O |
     * | G H I |          | P Q R |
    `</pre> *
     *
     *
     * Returns:
     *
     * <pre>`
     * | A B C |   | J K L |   | AJ+BM+CP AK+BN+CQ AL+BO+CR |
     * this * other = | D E F | * | M N O | = | DJ+EM+FP DK+EN+FQ DL+EO+FR |
     * | G H I |   | P Q R |   | GJ+HM+IP GK+HN+IQ GL+HO+IR |
    `</pre> *
     *
     * @param other  Matrix on right side of multiply expression
     * @return       this multiplied by other
     */
    fun makeConcat(other: Matrix33): Matrix33 {
        return Matrix33(
            mat[0] * other.mat[0] + mat[1] * other.mat[3] + mat[2] * other.mat[6],
            mat[0] * other.mat[1] + mat[1] * other.mat[4] + mat[2] * other.mat[7],
            mat[0] * other.mat[2] + mat[1] * other.mat[5] + mat[2] * other.mat[8],
            mat[3] * other.mat[0] + mat[4] * other.mat[3] + mat[5] * other.mat[6],
            mat[3] * other.mat[1] + mat[4] * other.mat[4] + mat[5] * other.mat[7],
            mat[3] * other.mat[2] + mat[4] * other.mat[5] + mat[5] * other.mat[8],
            mat[6] * other.mat[0] + mat[7] * other.mat[3] + mat[8] * other.mat[6],
            mat[6] * other.mat[1] + mat[7] * other.mat[4] + mat[8] * other.mat[7],
            mat[6] * other.mat[2] + mat[7] * other.mat[5] + mat[8] * other.mat[8]
        )
    }

    /**
     *
     * When converting from Matrix33 to Matrix44, the third row and
     * column remain as identity:
     *
     * <pre>`
     * [ a b c ]      [ a b 0 c ]
     * [ d e f ]  ->  [ d e 0 f ]
     * [ g h i ]      [ 0 0 1 0 ]
     * [ g h 0 i ]
    `</pre> *
     */
    fun asMatrix44(): Matrix44 {
        return Matrix44(mat[0], mat[1], 0f, mat[2], mat[3], mat[4], 0f, mat[5], 0f, 0f, 1f, 0f, mat[6], mat[7], 0f, mat[8])
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Matrix33) return false
        return mat.contentEquals(other.mat)
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + mat.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "Matrix33(_mat=$mat)"
    }

    companion object {
        /**
         * An identity Matrix33:
         *
         * <pre>`
         * | 1 0 0 |
         * | 0 1 0 |
         * | 0 0 1 |
        `</pre> *
         */
        val IDENTITY = makeTranslate(0f, 0f)

        /**
         *
         * Creates a Matrix33 to translate by (dx, dy). Returned matrix is:
         *
         * <pre>`
         * | 1 0 dx |
         * | 0 1 dy |
         * | 0 0  1 |
        `</pre> *
         *
         * @param dx  horizontal translation
         * @param dy  vertical translation
         * @return    Matrix33 with translation
         */
        fun makeTranslate(dx: Float, dy: Float): Matrix33 {
            return Matrix33(1f, 0f, dx, 0f, 1f, dy, 0f, 0f, 1f)
        }

        /**
         *
         * Creates a Matrix33 to scale by s. Returned matrix is:
         *
         * <pre>`
         * | s 0 0 |
         * | 0 s 0 |
         * | 0 0 1 |
        `</pre> *
         *
         * @param s  scale factor
         * @return   Matrix33 with scale
         */
        fun makeScale(s: Float): Matrix33 {
            return makeScale(s, s)
        }

        /**
         *
         * Creates a Matrix33 to scale by (sx, sy). Returned matrix is:
         *
         * <pre> `
         * | sx  0  0 |
         * |  0 sy  0 |
         * |  0  0  1 |
        `</pre> *
         *
         * @param sx horizontal scale factor
         * @param sy vertical scale factor
         * @return   Matrix33 with scale
         */
        fun makeScale(sx: Float, sy: Float): Matrix33 {
            return Matrix33(sx, 0f, 0f, 0f, sy, 0f, 0f, 0f, 1f)
        }

        /**
         * Creates a Matrix33 to rotate by |deg| about a pivot point at (0, 0).
         *
         * @param deg  rotation angle in degrees (positive rotates clockwise)
         * @return     Matrix33 with rotation
         */
        fun makeRotate(deg: Float): Matrix33 {
            val rad = deg.toRadians()
            var sin = sin(rad)
            var cos = cos(rad)
            val tolerance = (1.0f / (1 shl 12)).toDouble()
            if (abs(sin) <= tolerance) sin = 0.0
            if (abs(cos) <= tolerance) cos = 0.0
            return Matrix33(
                cos.toFloat(),
                (-sin).toFloat(),
                0f,
                sin.toFloat(),
                cos.toFloat(),
                0f,
                0f,
                0f,
                1f
            )
        }

        /**
         * Creates a Matrix33 to rotate by |deg| about a pivot point at pivot.
         *
         * @param deg    rotation angle in degrees (positive rotates clockwise)
         * @param pivot  pivot point
         * @return       Matrix33 with rotation
         */
        fun makeRotate(deg: Float, pivot: Point): Matrix33 {
            return makeRotate(deg, pivot.x, pivot.y)
        }

        /**
         * Creates a Matrix33 to rotate by |deg| about a pivot point at (pivotx, pivoty).
         *
         * @param deg     rotation angle in degrees (positive rotates clockwise)
         * @param pivotx  x-coord of pivot
         * @param pivoty  y-coord of pivot
         * @return        Matrix33 with rotation
         */
        fun makeRotate(deg: Float, pivotx: Float, pivoty: Float): Matrix33 {
            val rad = deg.toRadians()
            var sin = sin(rad)
            var cos = cos(rad)
            val tolerance = (1.0f / (1 shl 12)).toDouble()
            if (abs(sin) <= tolerance) sin = 0.0
            if (abs(cos) <= tolerance) cos = 0.0
            return Matrix33(
                cos.toFloat(),
                (-sin).toFloat(),
                (pivotx - pivotx * cos + pivoty * sin).toFloat(),
                sin.toFloat(),
                cos.toFloat(),
                (pivoty - pivoty * cos - pivotx * sin).toFloat(),
                0f,
                0f,
                1f
            )
        }

        /**
         *
         * Creates a Matrix33 to skew by (sx, sy). Returned matrix is:
         *
         * <pre> `
         * | 1  sx  0 |
         * | sy  1  0 |
         * |  0  0  1 |
        `</pre> *
         *
         * @param sx horizontal skew factor
         * @param sy vertical skew factor
         * @return   Matrix33 with skew
         */
        fun makeSkew(sx: Float, sy: Float): Matrix33 {
            return Matrix33(1f, sx, 0f, sy, 1f, 0f, 0f, 0f, 1f)
        }

        init {
            staticLoad()
        }

        // We defensively copy the preset arrays, as otherwise, Matrix33 would expose them to potential mutation:

        /** A toXYZD50 matrix to convert sRGB color into XYZ adapted to D50. Use it to create a color space. */
        val sRGBToXYZD50 get() = Matrix33(*_sRGBToXYZD50)

        /** A toXYZD50 matrix to convert Adobe RGB color into XYZ adapted to D50. Use it to create a color space. */
        val adobeRGBToXYZD50 get() = Matrix33(*_adobeRGBToXYZD50)

        /** A toXYZD50 matrix to convert Display P3 color into XYZ adapted to D50. Use it to create a color space. */
        val displayP3ToXYZD50 get() = Matrix33(*_displayP3ToXYZD50)

        /** A toXYZD50 matrix to convert Rec.2020 color into XYZ adapted to D50. Use it to create a color space. */
        val rec2020ToXYZD50 get() = Matrix33(*_rec2020ToXYZD50)

        /** A toXYZD50 identity matrix. Use it to create a color space. */
        val xyzD50ToXYZD50 get() = Matrix33(*_xyzD50ToXYZD50)

        private val _sRGBToXYZD50 = withResult(FloatArray(9)) { _nGetSRGB(it) }
        private val _adobeRGBToXYZD50 = withResult(FloatArray(9)) { _nGetAdobeRGB(it) }
        private val _displayP3ToXYZD50 = withResult(FloatArray(9)) { _nGetDisplayP3(it) }
        private val _rec2020ToXYZD50 = withResult(FloatArray(9)) { _nGetRec2020(it) }
        private val _xyzD50ToXYZD50 = withResult(FloatArray(9)) { _nGetXYZ(it) }

        /**
         * Returns a toXYZD50 matrix to adapt XYZ color from given the whitepoint to D50.
         * Use it to create a color space.
         *
         * @throws IllegalArgumentException If the white point is invalid.
         */
        fun makeXYZToXYZD50(wx: Float, wy: Float): Matrix33 {
            Stats.onNativeCall()
            val array = withNullableResult(FloatArray(9)) {
                _nAdaptToXYZD50(wx, wy, it)
            }
            requireNotNull(array) { "Cannot find transformation from the white point to D50" }
            return Matrix33(*array)
        }

        /**
         * Returns a toXYZD50 matrix to convert RGB color into XYZ adapted to D50,
         * given the primaries and whitepoint of the RGB model.
         * Use it to create a color space.
         *
         * @throws IllegalArgumentException If the primaries or white point are invalid.
         */
        fun makePrimariesToXYZD50(
            rx: Float, ry: Float, gx: Float, gy: Float, bx: Float, by: Float, wx: Float, wy: Float
        ): Matrix33 {
            Stats.onNativeCall()
            val array = withNullableResult(FloatArray(9)) {
                _nPrimariesToXYZD50(rx, ry, gx, gy, bx, by, wx, wy, it)
            }
            requireNotNull(array) { "Cannot find transformation from the primaries and white point to XYZ D50" }
            return Matrix33(*array)
        }
    }

    init {
        require(mat.size == 9) { "Expected 9 elements, got ${mat.size}" }
        this.mat = mat
    }
}

@ExternalSymbolName("org_jetbrains_skia_Matrix33__1nGetSRGB")
private external fun _nGetSRGB(result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Matrix33__1nGetAdobeRGB")
private external fun _nGetAdobeRGB(result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Matrix33__1nGetDisplayP3")
private external fun _nGetDisplayP3(result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Matrix33__1nGetRec2020")
private external fun _nGetRec2020(result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Matrix33__1nGetXYZ")
private external fun _nGetXYZ(result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Matrix33__1nAdaptToXYZD50")
private external fun _nAdaptToXYZD50(wx: Float, wy: Float, result: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Matrix33__1nPrimariesToXYZD50")
private external fun _nPrimariesToXYZD50(
    rx: Float, ry: Float, gx: Float, gy: Float, bx: Float, by: Float, wx: Float, wy: Float, result: InteropPointer
): Boolean
