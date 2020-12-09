package org.jetbrains.skiko

import org.jetbrains.skiko.Library

/**
 * The minimum required OpenGL constants and functions for Skia to work.
 * PS. In further development we should find a common pattern of using OpenGL,
 * Vulkan, Metal and implement common interface to all graphics APIs.
 */
class OpenGLApi private constructor() {
    // OpenGL constants
    val GL_TEXTURE_2D = 0x0DE1
    val GL_TEXTURE_BINDING_2D = 0x8069
    val GL_DRAW_FRAMEBUFFER_BINDING = 0x8CA6
    val GL_COLOR_BUFFER_BIT = 0x00004000

    // OpenGL functions
    external fun glViewport(x: Int, y: Int, width: Int, height: Int)
    external fun glClearColor(r: Float, g: Float, b: Float, a: Float)
    external fun glClear(flags: Int)
    external fun glFinish()
    external fun glEnable(flag: Int)
    external fun glBindTexture(target: Int, texture: Int)
    external fun glGetIntegerv(pname: Int): Int

    companion object {
        init {
            Library.load()
        }
        val instance = OpenGLApi()
    }
}