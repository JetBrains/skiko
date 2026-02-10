package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class ColorMatrix(val mat: FloatArray) {
    constructor(
        m00: Float, m01: Float, m02: Float, m03: Float, m04: Float,
        m10: Float, m11: Float, m12: Float, m13: Float, m14: Float,
        m20: Float, m21: Float, m22: Float, m23: Float, m24: Float,
        m30: Float, m31: Float, m32: Float, m33: Float, m34: Float
    ) : this(
        floatArrayOf(
            m00, m01, m02, m03, m04,
            m10, m11, m12, m13, m14,
            m20, m21, m22, m23, m24,
            m30, m31, m32, m33, m34
        )
    )

    init {
        require(mat.size == 20) { "Expected 20 elements, got ${mat.size}" }
    }

    override fun toString(): String {
        return "ColorMatrix(mat=${mat.contentToString()})"
    }
}