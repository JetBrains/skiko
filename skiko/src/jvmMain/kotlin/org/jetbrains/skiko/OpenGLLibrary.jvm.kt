package org.jetbrains.skiko

import org.jetbrains.skia.impl.Library

private external fun loadOpenGLLibraryWindows()

private var isLoaded = false

@Synchronized
internal actual fun loadOpenGLLibrary() {
    if (!isLoaded) {
        when {
            // On Windows it is linked dynamically
            hostOs.isWindows -> {
                Library.staticLoad()
                loadOpenGLLibraryWindows()
            }
            // it was deprecated in macOS 10.14
            // see https://developer.apple.com/library/archive/documentation/GraphicsImaging/Conceptual/OpenGL-MacProgGuide/opengl_intro/opengl_intro.html
            hostOs.isMacOS -> {
                if (!SkikoProperties.macOsOpenGLEnabled) {
                    throw RenderException("OpenGL on macOS is deprecated. To enable its support, call System.setProperty(\"skiko.macos.opengl.enabled\", \"true\")")
                }
            }
            // Do nothing as we don't know for sure which OS supports OpenGL.
            // If there is support, we assume that it is already linked.
            // If there is no support, there will be a native crash
            // (to throw a RenderException we need to investigate case by case)
            else -> Unit
        }
        isLoaded = true
    }
}
