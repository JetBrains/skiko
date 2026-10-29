package org.jetbrains.skia

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
value class FourByteTag(val tag : Int){
    companion object {
        @JvmStatic
        fun fromString(name: String): FourByteTag {

            require(name.length == 4) { "Name must be exactly 4 symbols, got: '$name'" }
            return FourByteTag(
                name[0].code and 0xFF shl 24 or (name[1].code and 0xFF shl 16
                        ) or (name[2].code and 0xFF shl 8
                        ) or (name[3].code and 0xFF)
            )
        }

        @JvmStatic
        fun toString(tag: FourByteTag): String {
            return charArrayOf(
                (tag.tag shr 24 and 0xFF).toChar(),
                (tag.tag shr 16 and 0xFF).toChar(),
                (tag.tag shr 8 and 0xFF).toChar(),
                (tag.tag and 0xFF).toChar()
            ).concatToString()
        }
    }
}