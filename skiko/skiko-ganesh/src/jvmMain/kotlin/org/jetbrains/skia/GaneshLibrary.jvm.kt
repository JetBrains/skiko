package org.jetbrains.skia

import org.jetbrains.skiko.Library
import org.jetbrains.skiko.LibraryLoader
import org.jetbrains.skiko.InternalSkikoApi
import org.jetbrains.skiko.hostId

private val ganeshLoader = LibraryLoader("skiko-ganesh-$hostId")

@InternalSkikoApi
actual object GaneshLibrary {
    @Synchronized
    actual fun load() {
        Library.load()
        ganeshLoader.loadOnce()
    }
}