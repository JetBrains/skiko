package org.jetbrains.skia.skottie

import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

fun AnimationBuilder.buildFromFile(path: String): Animation {
    return try {
        Stats.onNativeCall()
        val ptr = interopScope { _nBuildFromFile(_ptr, toInterop(path)) }
        require(ptr != Native.NullPointer) { "Failed to create Animation from path: $path" }
        Animation(ptr)
    } finally {
        reachabilityBarrier(this)
    }
}