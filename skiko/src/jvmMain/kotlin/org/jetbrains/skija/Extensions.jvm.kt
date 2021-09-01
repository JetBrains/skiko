package org.jetbrains.skija

class Extensions {
    companion object {
        @JvmStatic
        external fun _nIsAlwaysOpaque(value: Int): Boolean
    }
}

/**
 * Returns true if ColorType always decodes alpha to 1.0, making the pixel
 * fully opaque. If true, ColorType does not reserve bits to encode alpha.
 *
 * @return  true if alpha is always set to 1.0
 */
val ColorType.isAlwaysOpaque: Boolean
    get() {
        return Extensions._nIsAlwaysOpaque(ordinal)
    }



