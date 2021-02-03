package org.jetbrains.skiko

import kotlin.math.nextDown
import kotlin.math.roundToInt

/**
 * Restore the actual dimension in pixels after it was scaled by AWT.
 *
 * This formula is determined experimental way, but works fine on macOs/Windows/Linux
 * with hardware/software rendering, on various DPI's, on OpenJDK 15 / Java HotSpot 11.
 *
 * If we use just toInt() or roundToInt() we will have artifacts on resize on Windows with fractional DPI
 * (it is wrongly stretched with software render, and translated with hardware render).
 */
internal fun unscaledAWTDimension(
    scaledValue: Int,
    contentScale: Float
) = (scaledValue * contentScale).nextDown().roundToInt().coerceAtLeast(0)