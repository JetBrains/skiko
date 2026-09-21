package org.jetbrains.skiko

internal object AngleApi {
    // OpenGL constants
    val GL_VENDOR = 0x1F00
    val GL_RENDERER = 0x1F01
    val GL_VERSION = 0x1F02

    // OpenGL functions
    external fun glGetString(value: Int): String?
}
