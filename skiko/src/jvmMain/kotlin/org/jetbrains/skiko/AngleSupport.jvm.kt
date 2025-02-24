package org.jetbrains.skiko

import org.jetbrains.skia.impl.Library

private external fun loadAngleLibraryWindows()

private var isLoaded = false

@Synchronized
internal actual fun loadAngleLibrary() {
    if (!isLoaded) {
        when {
            hostOs.isWindows -> {
                Library.staticLoad()
                try {
                    loadAngleLibraryWindows()
                }
                catch (e: Exception) {
                    throw RenderException("Failed to load ANGLE library: ${e}")
                }
            }
            else -> Unit
        }
        isLoaded = true
    }
}