package org.jetbrains.skiko

// Clicked mouse buttons bitmask.
object SkikoMouseButtons {
    const val NONE = 0
    const val LEFT = 1 shl 0
    const val RIGHT = 1 shl 1
    const val MIDDLE = 1 shl 2
}

expect class SkikoPlatformInputEvent
data class SkikoInputEvent(
    val input: String,
    val platform: SkikoPlatformInputEvent?
)

enum class SkikoKeyboardEventKind {
    UP, DOWN
}
expect class SkikoPlatformKeyboardEvent
data class SkikoKeyboardEvent(
    val code: Int,
    val kind: SkikoKeyboardEventKind,
    val platform: SkikoPlatformKeyboardEvent?
)

enum class SkikoPointerEventKind {
    UP, DOWN, MOVE
}
expect class SkikoPlatformPointerEvent
data class SkikoPointerEvent(
    val x: Double, val y: Double,
    val buttonMask: Int,
    val kind: SkikoPointerEventKind,
    val platform: SkikoPlatformPointerEvent?
)

val SkikoPointerEvent.isLeftClick: Boolean
    get() = (buttonMask and SkikoMouseButtons.LEFT) != 0 && (kind == SkikoPointerEventKind.UP)

val SkikoPointerEvent.isRightClick: Boolean
    get() = (buttonMask and SkikoMouseButtons.RIGHT) != 0 && (kind == SkikoPointerEventKind.UP)

