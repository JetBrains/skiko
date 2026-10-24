package org.jetbrains.skiko.internal

actual inline fun floatFromBits(bits: Int): Float = java.lang.Float.intBitsToFloat(bits)