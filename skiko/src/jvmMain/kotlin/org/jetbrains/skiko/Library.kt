package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap
import java.util.concurrent.atomic.AtomicBoolean

object Library {
    private var loader = LibraryLoader(
        name = "skiko-$hostId",
        additionalFile = if (hostOs.isWindows) "icudtl.dat" else null,
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
