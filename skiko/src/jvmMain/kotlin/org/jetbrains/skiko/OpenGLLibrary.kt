package org.jetbrains.skiko

/**
 * Load OpenGL library into memory.
 *
 * Should be called before any OpenGL operation.
 *
 * The current implementation loads OpenGl lazily on Windows, and at app startup on Linux/macOs.
 *
 * Some Windows machines don't have OpenGL (usually CI machines), so we'll fallback to other renderers on them.
 *
 * @return false if there is no OpenGL library in the system.
 */
internal fun loadOpenGLLibrary(): Boolean {
    return if (hostOs.isWindows) {
        loadOpenGLLibraryWindows()
    } else {
        true
    }
}

@Synchronized
private external fun loadOpenGLLibraryWindows(): Boolean