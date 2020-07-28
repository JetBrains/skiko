package org.jetbrains.skiko

import org.jetbrains.skija.Library
import org.jetbrains.skija.Surface

fun main() {
   println(SkiaWindow().nativeMethod(41))
   val surface = Surface.makeRasterN32Premul(100, 100)
   println(surface)
}