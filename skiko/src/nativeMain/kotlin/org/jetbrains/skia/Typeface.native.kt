package org.jetbrains.skia

import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope

/**
 * @return  a new typeface given a file
 * @throws IllegalArgumentException  If the file does not exist, or is not a valid font file
 */
fun Typeface.Companion.makeFromFile(path: String, index: Int = 0): Typeface {
    Stats.onNativeCall()
    interopScope {
        val ptr = _nMakeFromFile(toInterop(path), index)
        require(ptr != Native.NullPointer) { "Failed to create Typeface from path=\"$path\" index=$index" }
        return Typeface(ptr)
    }
}