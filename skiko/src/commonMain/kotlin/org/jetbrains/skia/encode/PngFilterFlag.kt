package org.jetbrains.skia.encode

import kotlin.jvm.JvmInline

@JvmInline
value class PngFilterFlag internal constructor(val bits: Int) {
    infix fun or(other: PngFilterFlag): PngFilterFlag {
        return PngFilterFlag(bits or other.bits)
    }

    companion object {
        val ZERO = PngFilterFlag(0x00)
        val NONE = PngFilterFlag(0x08)
        val SUB = PngFilterFlag(0x10)
        val UP = PngFilterFlag(0x20)
        val AVG = PngFilterFlag(0x40)
        val PAETH = PngFilterFlag(0x80)
        val ALL = NONE or SUB or UP or AVG or PAETH
    }
}
