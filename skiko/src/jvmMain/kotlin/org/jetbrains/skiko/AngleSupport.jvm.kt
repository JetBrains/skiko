package org.jetbrains.skiko

import org.jetbrains.skia.impl.Library

private external fun initAngleLibraryWindows(libraryName: String): Boolean

private const val libEGLName = "libEGL"

private var loader = LibraryLoader(
    libEGLName,
    // libGLESv2 is not loaded explicitly in Skiko, it is loaded by libEGL
    additionalFile = System.mapLibraryName("libGLESv2"),
    init = {
        Library.staticLoad()
        if (!initAngleLibraryWindows(libEGLName)) {
            throw LibraryLoadException("Failed to load ANGLE library $libEGLName")
        }
    }
)

internal actual fun loadAngleLibrary() {
    when {
        hostOs.isWindows -> loader.loadOnce()
        else -> Unit
    }
}
