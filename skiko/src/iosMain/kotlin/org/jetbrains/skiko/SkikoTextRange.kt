package org.jetbrains.skiko

/**
 * A range of characters in a text container with a starting index and an ending index in string backing a text-entry object.
 * https://developer.apple.com/documentation/uikit/uitextrange
 * @param start inclusive
 * @param end exclusive
 */
data class SkikoTextRange(val start: Int, val end: Int)

val SkikoTextRange.length: Int get() = end - start

