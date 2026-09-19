package org.jetbrains.skia.skottie

import org.jetbrains.skiko.Library
import org.jetbrains.skiko.LibraryLoader
import org.jetbrains.skiko.hostId

private val skottieLoader = LibraryLoader(
    name = "skiko-skottie-$hostId",
    init = {
        try {
            _nAfterLoad()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
)

internal actual object SkottieLibrary {
    actual fun load() {
        Library.load()
        skottieLoader.loadOnce()
    }
}

private external fun _nAfterLoad()
