package org.jetbrains.skiko

private val renderingLoader = LibraryLoader(
    name = "skiko-rendering-$hostId",
    init = { Setup.init() },
)

internal object RenderingLibrary {
    fun load() {
        Library.load()
        renderingLoader.loadOnce()
    }
}
