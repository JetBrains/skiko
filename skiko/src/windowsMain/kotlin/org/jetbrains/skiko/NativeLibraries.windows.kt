@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.jetbrains.skiko

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.memScoped
import platform.windows.GetLastError
import platform.windows.LoadLibraryW

private val loadedNativeLibraries = mutableMapOf<String, COpaquePointer>()

private fun loadNativeLibrary(name: String): COpaquePointer {
    loadedNativeLibraries[name]?.let { return it }

    val library = memScoped { LoadLibraryW(name) }
        ?: throw RenderException("Unable to load $name (Win32 error ${GetLastError()})")

    loadedNativeLibraries[name] = library
    return library
}

internal actual fun loadAngleLibrary() {
    try {
        loadNativeLibrary("libEGL.dll")
        loadNativeLibrary("libGLESv2.dll")
    } catch (error: RenderException) {
        throw RenderException("ANGLE libraries are unavailable", error)
    }
}

internal actual fun loadOpenGLLibrary() {
    loadNativeLibrary("opengl32.dll")
}
