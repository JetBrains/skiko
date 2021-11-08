package org.jetbrains.skia.skottie

import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope


fun Animation.Companion.makeFromFile(path: String): Animation {
    Stats.onNativeCall()
    interopScope {
        val ptr = _nMakeFromFile(toInterop(path))
        require(ptr != Native.NullPointer) { "Failed to create Animation from path=\"$path\"" }
        return Animation(ptr)
    }
}