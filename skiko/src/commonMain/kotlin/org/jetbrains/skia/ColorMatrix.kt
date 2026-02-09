package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class ColorMatrix(val mat: FloatArray) {
    constructor(vararg mat: Float) : this(mat)

    init {
        require(mat.size == 20) { "Expected 20 elements, got ${mat.size}" }
    }

    override fun toString(): String {
        return "ColorMatrix(mat=${mat.contentToString()})"
    }
}