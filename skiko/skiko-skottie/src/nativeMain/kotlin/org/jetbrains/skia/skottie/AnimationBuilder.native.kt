package org.jetbrains.skia.skottie

import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.nativeCall

fun AnimationBuilder.buildFromFile(path: String): Animation = nativeCall {
    interopScope {
        val ptr = _nBuildFromFile(nativePtr, toInterop(path))
        require(ptr != Native.NullPointer) { "Failed to create Animation from path: $path" }
        Animation(ptr)
    }
}
