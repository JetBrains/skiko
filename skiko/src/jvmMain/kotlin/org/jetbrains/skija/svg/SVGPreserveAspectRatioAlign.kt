package org.jetbrains.skija.svg

import java.lang.IllegalArgumentException

enum class SVGPreserveAspectRatioAlign constructor(internal val _value: Int) {
    // These values are chosen such that bits [0,1] encode X alignment, and
    // bits [2,3] encode Y alignment.
    XMIN_YMIN(0x00), XMID_YMIN(0x01), XMAX_YMIN(0x02), XMIN_YMID(0x04), XMID_YMID(0x05), XMAX_YMID(0x06), XMIN_YMAX(0x08), XMID_YMAX(
        0x09
    ),
    XMAX_YMAX(0x0a), NONE(0x10);

    companion object {
        internal fun valueOf(value: Int): SVGPreserveAspectRatioAlign {
            return when (value) {
                0x00 -> XMIN_YMIN
                0x01 -> XMID_YMIN
                0x02 -> XMAX_YMIN
                0x04 -> XMIN_YMID
                0x05 -> XMID_YMID
                0x06 -> XMAX_YMID
                0x08 -> XMIN_YMAX
                0x09 -> XMID_YMAX
                0x0a -> XMAX_YMAX
                0x10 -> NONE
                else -> throw IllegalArgumentException("Unknown SVGPreserveAspectRatioAlign value: $value")
            }
        }
    }
}