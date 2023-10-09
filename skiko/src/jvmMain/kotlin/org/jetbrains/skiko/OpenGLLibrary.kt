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
 * Throws [RenderException] if OpenGL library can't be loaded.
 */
internal fun loadOpenGLLibrary() {
    if (hostOs.isWindows) {
        loadOpenGLLibraryWindows()
    } else {
        // do nothing, the library should be already available
    }
}

@Synchronized
private external fun loadOpenGLLibraryWindows()