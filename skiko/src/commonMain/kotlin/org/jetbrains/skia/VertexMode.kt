package org.jetbrains.skia

import kotlin.jvm.JvmInline


@JvmInline
value class VertexMode internal constructor(val ordinal: Int){
    companion object{
        val TRIANGLES = VertexMode(0)
        val TRIANGLE_STRIP = VertexMode(1)
        val TRIANGLE_FAN = VertexMode(2)
    }
}
