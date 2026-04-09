package org.jetbrains.skia.svg

import kotlin.jvm.JvmInline

@JvmInline
value class SVGLengthUnit internal constructor(val ordinal: Int) {
    companion object {
         val UNKNOWN = SVGLengthUnit(0)
         val NUMBER = SVGLengthUnit(1)
         val PERCENTAGE = SVGLengthUnit(2)
         val EMS = SVGLengthUnit(3)
         val EXS = SVGLengthUnit(4)
         val PX = SVGLengthUnit(5)
         val CM = SVGLengthUnit(6)
         val MM = SVGLengthUnit(7)
         val IN = SVGLengthUnit(8)
         val PT = SVGLengthUnit(9)
         val PC = SVGLengthUnit(10)
    }
}