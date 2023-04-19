package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.Library.Companion.staticLoad

enum class Direction {
    RTL,
    LTR;
}

/**
 * Returns the bidirectional category value for the code point.
 *
 * See ICU's u_charDirection
 * Same as java.lang.Character.getDirectionality()
 */
fun charDirectionality(codePoint: Int): Direction? {
    staticLoad()
    return when (unicodeCharDirection(codePoint)) {
        U_RIGHT_TO_LEFT,
        U_RIGHT_TO_LEFT_ARABIC,
        U_RIGHT_TO_LEFT_EMBEDDING,
        U_RIGHT_TO_LEFT_OVERRIDE -> Direction.RTL

        U_LEFT_TO_RIGHT,
        U_LEFT_TO_RIGHT_EMBEDDING,
        U_LEFT_TO_RIGHT_OVERRIDE -> Direction.LTR

        else -> null
    }
}

private const val U_LEFT_TO_RIGHT = 0
private const val U_RIGHT_TO_LEFT = 1
private const val U_LEFT_TO_RIGHT_EMBEDDING     = 11
private const val U_LEFT_TO_RIGHT_OVERRIDE      = 12
private const val U_RIGHT_TO_LEFT_ARABIC        = 13
private const val U_RIGHT_TO_LEFT_EMBEDDING     = 14
private const val U_RIGHT_TO_LEFT_OVERRIDE      = 15

@ExternalSymbolName("org_jetbrains_skia_paragraph_Direction_unicodeCharDirection")
private external fun unicodeCharDirection(codePoint: Int): Int
