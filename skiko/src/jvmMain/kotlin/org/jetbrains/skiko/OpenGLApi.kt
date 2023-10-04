package org.jetbrains.skiko

/**
 * The minimum required OpenGL constants and functions for Skia to work.
 * PS. In further development we should find a common pattern of using OpenGL,
 * Vulkan, Metal and implement common interface to all graphics APIs.
 */
internal class OpenGLApi private constructor() {
    // OpenGL constants
    val GL_DRAW_FRAMEBUFFER_BINDING = 0x8CA6
    val GL_VENDOR = 0x1F00
    val GL_RENDERER = 0x1F01
    val GL_TOTAL_MEMORY = 0x9048

    // OpenGL functions
    external fun glFinish()
    external fun glGetIntegerv(pname: Int): Int
    external fun glGetString(value: Int): String?

    companion object {
        init {
            Library.load()
        }
        val instance = OpenGLApi()
    }
}