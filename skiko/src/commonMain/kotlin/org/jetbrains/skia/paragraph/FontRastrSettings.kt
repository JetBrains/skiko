package org.jetbrains.skia.paragraph

import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontHinting

@Deprecated(
    message = "Replaced by separate properties in TextStyle: edging, hinting, subpixel",
    level = DeprecationLevel.ERROR,
)
data class FontRastrSettings(val edging: FontEdging,
                             val hinting: FontHinting,
                             val subpixel: Boolean)
