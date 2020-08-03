package org.jetbrains.skiko

import org.jetbrains.skija.Library
import org.jetbrains.skija.Surface

fun main() {
   println(SkiaLibraryTester().nativeMethod(41))
   val surface = Surface.makeRasterN32Premul(100, 100)
   println(surface)
}

private class SkiaLibraryTester {
    companion object {
        init {
          Library.load("/", "skiko")
        }
    }

    external fun nativeMethod(param: Long): Long
}