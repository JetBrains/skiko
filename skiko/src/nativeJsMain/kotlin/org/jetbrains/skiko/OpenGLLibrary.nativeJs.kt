package org.jetbrains.skiko

internal actual fun loadOpenGLLibrary() {
    when (hostOs) {
        // It is deprecated. See https://developer.apple.com/documentation/opengles
        OS.Ios -> throw RenderException("OpenGL on iOS isn't supported")
        // Do nothing as we don't know for sure which OS supports OpenGL.
        // If there is support, we assume that it is already linked.
        // If there is no support, there will be a native crash
        // (to throw a RenderException we need to investigate case by case)
        else -> Unit
    }
}