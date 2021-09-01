package org.jetbrains.skija

interface FourByteTag {
    companion object {
        fun fromString(name: String): Int {
            require(name.length == 4) { "Name must be exactly 4 symbols, got: '$name'" }
            return name[0].code and 0xFF shl 24 or (name[1].code and 0xFF shl 16
                    ) or (name[2].code and 0xFF shl 8
                    ) or (name[3].code and 0xFF)
        }

        fun toString(tag: Int): String {
            return charArrayOf(
                    (tag shr 24 and 0xFF).toChar(),
                    (tag shr 16 and 0xFF).toChar(),
                    (tag shr 8 and 0xFF).toChar(),
                    (tag and 0xFF).toChar()
                ).concatToString()
        }
    }
}