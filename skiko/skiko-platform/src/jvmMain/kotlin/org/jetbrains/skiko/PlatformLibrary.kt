package org.jetbrains.skiko

private val platformLoader = LibraryLoader(
    name = "skiko-platform-$hostId",
    init = { Setup.init() },
    cls = PlatformLibrary::class.java,
)

internal object PlatformLibrary {
    fun load() {
        Library.load()
        platformLoader.loadOnce()
    }
}
