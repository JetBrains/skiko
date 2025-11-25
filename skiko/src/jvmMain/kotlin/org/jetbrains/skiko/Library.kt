package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap

object Library {
    internal val name: String = "skiko-$hostId"

    private var loader = LibraryLoader(
        name = name,
        additionalFile = if (hostOs.isWindows) "icudtl.dat" else null,
        lockFile = LockFile.skiko,
        init = {
            Setup.init()

            try {
                // Init code executed after library was loaded.
                org.jetbrains.skia.impl.Library._nAfterLoad()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    )

    // This function does the following: on request to load given resource,
    // it checks if resource with given name is found in content-derived directory
    // in Skiko's home, and if not - unpacks it. It could also load additional
    // localization resources, on platforms where it is needed.
    fun load() {
        loader.loadOnce()
    }
}

// We have to keep this tiny class in Skiko for testing purposes.
internal class LibraryTestImpl() {
    fun run(): Long {
        val bitmap = Bitmap()
        return bitmap._ptr
    }
}
