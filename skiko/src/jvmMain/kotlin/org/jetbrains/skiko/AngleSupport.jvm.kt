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
                loadAngleLibraryWindows()
            }
            else -> Unit
        }
        isLoaded = true
    }
}