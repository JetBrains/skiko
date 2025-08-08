package org.jetbrains.skiko

import org.jetbrains.skia.impl.Library
import org.jetbrains.skiko.Library.findAndLoadExact

private external fun loadAngleLibraryWindows(eglPath: String, glesPath: String)

private var isLoaded = false

@Synchronized
internal actual fun loadAngleLibrary() {
    if (!isLoaded) {
        when {
            hostOs.isWindows -> {
                Library.staticLoad()
                try {
                    val eglResult = findAndLoadExact("skiko-angle-libEGL-$hostId.dll")
                    val glesResult = findAndLoadExact("skiko-angle-libGLESv2-$hostId.dll")
                    loadAngleLibraryWindows(
                        eglResult.file.absolutePath,
                        glesResult.file.absolutePath
                    )
                } catch (e: Throwable) {
                    throw OptionalRenderApiException("Failed to load ANGLE runtime: ${e.message}")
                }
            }
            else -> Unit
        }
        isLoaded = true
    }
}
