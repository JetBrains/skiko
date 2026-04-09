package org.jetbrains.skia

import kotlin.jvm.JvmInline


@JvmInline
value class FontSlant internal constructor(val ordinal : Int){
    companion object{
        val UPRIGHT = FontSlant(0)
        val ITALIC = FontSlant(1)
        val OBLIQUE = FontSlant(2)
    }
}