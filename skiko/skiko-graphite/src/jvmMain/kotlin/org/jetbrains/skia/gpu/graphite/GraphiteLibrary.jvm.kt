package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skiko.Library
import org.jetbrains.skiko.LibraryLoader
import org.jetbrains.skiko.hostId

private val graphiteLoader = LibraryLoader("skiko-graphite-$hostId")

internal actual object GraphiteLibrary {
    actual fun load() {
        Library.load()
        graphiteLoader.loadOnce()
    }
}
