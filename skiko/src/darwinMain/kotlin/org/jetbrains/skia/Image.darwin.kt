package org.jetbrains.skia

import platform.Foundation.NSData
import kotlin.native.internal.NativePtr

fun Image.Companion.makeFromEncoded(nsData: NSData): Image {
    val ptr = nsData.bytes?.rawValue ?: NativePtr.NULL
    require(ptr != NativePtr.NULL) { "Failed to Image::makeFromEncoded" }

    // skia makes an internal copy of the nsData bytes
    val imgPtr = _nMakeFromEncoded(ptr, nsData.length.toInt())
    require(imgPtr != NativePtr.NULL) { "Failed to Image::makeFromEncoded" }
    return Image(imgPtr)
}