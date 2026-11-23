package org.jetbrains.skia

enum class GLBackendState(internal val _bit: Int) {
    RENDER_TARGET(1 shl 0),

    /**
     * Also includes samplers bound to texture units.
     */
    TEXTURE_BINDING(1 shl 1),

    /**
     * View state stands for scissor and viewport
     */
    VIEW(1 shl 2),
    BLEND(1 shl 3),
    MSAA_ENABLE(1 shl 4),
    VERTEX(1 shl 5),
    STENCIL(1 shl 6),
    PIXEL_STORE(1 shl 7),
    PROGRAM(1 shl 8),
    FIXED_FUNCTION(1 shl 9),
    MISC(1 shl 10),
    PATH_RENDERING(1 shl 11);
}