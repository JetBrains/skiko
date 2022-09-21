package org.jetbrains.skia.paragraph

import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontHinting

data class FontRasterSettings(val edging: FontEdging, val hinting: FontHinting, val subpixel: Boolean)