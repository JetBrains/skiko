package org.jetbrains.skiko

internal actual fun loadAngleLibrary() = Unit

internal actual fun loadOpenGLLibrary() {
    throw RenderException("OpenGL on iOS and tvOS isn't supported")
}
