package org.jetbrains.skija

interface FramebufferFormat {
    companion object {
        const val GR_GL_STENCIL_INDEX = 0x1901
        const val GR_GL_DEPTH_COMPONENT = 0x1902
        const val GR_GL_DEPTH_STENCIL = 0x84F9
        const val GR_GL_RED = 0x1903
        const val GR_GL_RED_INTEGER = 0x8D94
        const val GR_GL_GREEN = 0x1904
        const val GR_GL_BLUE = 0x1905
        const val GR_GL_ALPHA = 0x1906
        const val GR_GL_LUMINANCE = 0x1909
        const val GR_GL_LUMINANCE_ALPHA = 0x190A
        const val GR_GL_RG_INTEGER = 0x8228
        const val GR_GL_RGB = 0x1907
        const val GR_GL_RGB_INTEGER = 0x8D98
        const val GR_GL_SRGB = 0x8C40
        const val GR_GL_RGBA = 0x1908
        const val GR_GL_RG = 0x8227
        const val GR_GL_SRGB_ALPHA = 0x8C42
        const val GR_GL_RGBA_INTEGER = 0x8D99
        const val GR_GL_BGRA = 0x80E1

        /* Stencil index sized formats */
        const val GR_GL_STENCIL_INDEX4 = 0x8D47
        const val GR_GL_STENCIL_INDEX8 = 0x8D48
        const val GR_GL_STENCIL_INDEX16 = 0x8D49

        /* Depth component sized formats */
        const val GR_GL_DEPTH_COMPONENT16 = 0x81A5

        /* Depth stencil sized formats */
        const val GR_GL_DEPTH24_STENCIL8 = 0x88F0

        /* Red sized formats */
        const val GR_GL_R8 = 0x8229
        const val GR_GL_R16 = 0x822A
        const val GR_GL_R16F = 0x822D
        const val GR_GL_R32F = 0x822E

        /* Red integer sized formats */
        const val GR_GL_R8I = 0x8231
        const val GR_GL_R8UI = 0x8232
        const val GR_GL_R16I = 0x8233
        const val GR_GL_R16UI = 0x8234
        const val GR_GL_R32I = 0x8235
        const val GR_GL_R32UI = 0x8236

        /* Luminance sized formats */
        const val GR_GL_LUMINANCE8 = 0x8040
        const val GR_GL_LUMINANCE16F = 0x881E

        /* Alpha sized formats */
        const val GR_GL_ALPHA8 = 0x803C
        const val GR_GL_ALPHA16 = 0x803E
        const val GR_GL_ALPHA16F = 0x881C
        const val GR_GL_ALPHA32F = 0x8816

        /* Alpha integer sized formats */
        const val GR_GL_ALPHA8I = 0x8D90
        const val GR_GL_ALPHA8UI = 0x8D7E
        const val GR_GL_ALPHA16I = 0x8D8A
        const val GR_GL_ALPHA16UI = 0x8D78
        const val GR_GL_ALPHA32I = 0x8D84
        const val GR_GL_ALPHA32UI = 0x8D72

        /* RG sized formats */
        const val GR_GL_RG8 = 0x822B
        const val GR_GL_RG16 = 0x822C

        // int GR_GL_R16F                         = 0x822D;
        // int GR_GL_R32F                         = 0x822E;
        const val GR_GL_RG16F = 0x822F

        /* RG sized integer formats */
        const val GR_GL_RG8I = 0x8237
        const val GR_GL_RG8UI = 0x8238
        const val GR_GL_RG16I = 0x8239
        const val GR_GL_RG16UI = 0x823A
        const val GR_GL_RG32I = 0x823B
        const val GR_GL_RG32UI = 0x823C

        /* RGB sized formats */
        const val GR_GL_RGB5 = 0x8050
        const val GR_GL_RGB565 = 0x8D62
        const val GR_GL_RGB8 = 0x8051
        const val GR_GL_SRGB8 = 0x8C41

        /* RGB integer sized formats */
        const val GR_GL_RGB8I = 0x8D8F
        const val GR_GL_RGB8UI = 0x8D7D
        const val GR_GL_RGB16I = 0x8D89
        const val GR_GL_RGB16UI = 0x8D77
        const val GR_GL_RGB32I = 0x8D83
        const val GR_GL_RGB32UI = 0x8D71

        /* RGBA sized formats */
        const val GR_GL_RGBA4 = 0x8056
        const val GR_GL_RGB5_A1 = 0x8057
        const val GR_GL_RGBA8 = 0x8058
        const val GR_GL_RGB10_A2 = 0x8059
        const val GR_GL_SRGB8_ALPHA8 = 0x8C43
        const val GR_GL_RGBA16F = 0x881A
        const val GR_GL_RGBA32F = 0x8814
        const val GR_GL_RG32F = 0x8230
        const val GR_GL_RGBA16 = 0x805B

        /* RGBA integer sized formats */
        const val GR_GL_RGBA8I = 0x8D8E
        const val GR_GL_RGBA8UI = 0x8D7C
        const val GR_GL_RGBA16I = 0x8D88
        const val GR_GL_RGBA16UI = 0x8D76
        const val GR_GL_RGBA32I = 0x8D82
        const val GR_GL_RGBA32UI = 0x8D70

        /* BGRA sized formats */
        const val GR_GL_BGRA8 = 0x93A1
    }
}