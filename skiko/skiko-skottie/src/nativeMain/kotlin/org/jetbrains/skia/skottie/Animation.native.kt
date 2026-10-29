package org.jetbrains.skia.skottie

import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.nativeCallFromStatic


fun Animation.Companion.makeFromFile(path: String): Animation = nativeCallFromStatic {
    interopScope {
        val ptr = _nMakeFromFile(toInterop(path))
        require(ptr != Native.NullPointer) { "Failed to create Animation from path=\"$path\"" }
        Animation(ptr)
    }
}
