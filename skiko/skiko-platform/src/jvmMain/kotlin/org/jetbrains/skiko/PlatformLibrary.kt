package org.jetbrains.skiko

import org.jetbrains.skia.GaneshLibrary

private val platformLoader = LibraryLoader(
    name = "skiko-platform-$hostId",
    init = { Setup.init() },
    cls = PlatformLibrary::class.java,
)

internal object PlatformLibrary {
    fun load() {
        GaneshLibrary.load()
        platformLoader.loadOnce()
    }
}
