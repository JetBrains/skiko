package org.jetbrains.skiko

import platform.windows.*

private fun LOWORD(lParam: LPARAM): UInt = lParam.toUInt() and 0xffffu
private fun HIWORD(lParam: LPARAM): UInt = lParam.toUInt() shr 16

private fun LOBYTE(lParam: LPARAM): UInt = lParam.toUInt() and 0xffu
private fun MAKEWORD(low: UInt, high: UInt): UInt = low or (high shl 16)

operator fun SkikoMouseButtons.plus(other: SkikoMouseButtons): SkikoMouseButtons =
    SkikoMouseButtons(this.value or other.value)

operator fun SkikoInputModifiers.plus(other: SkikoInputModifiers): SkikoInputModifiers =
    SkikoInputModifiers(this.value or other.value)

operator fun SkikoInputModifiers.minus(other: SkikoInputModifiers): SkikoInputModifiers =
    SkikoInputModifiers(this.value and other.value.inv())

actual data class SkikoPlatformKeyboardEvent(
    val kind: SkikoKeyboardEventKind,
    val virtualKey: WPARAM,
    val modifiers: LPARAM,
    val inputModifiers: SkikoInputModifiers
) {
    val skikoEvent: SkikoKeyboardEvent
        get() {
            var vk = virtualKey.toInt()
            if (vk == VK_SHIFT || vk == VK_CONTROL || vk == VK_MENU) {
                val keyFlags = HIWORD(modifiers)
                val isExtendedKey = (keyFlags and KF_EXTENDED.toUInt()) == KF_EXTENDED.toUInt()
                if (isExtendedKey) {
                    when (vk) {
                        VK_SHIFT -> vk = VK_RSHIFT
                        VK_CONTROL -> vk = VK_RCONTROL
                        VK_MENU -> vk = VK_RMENU
                    }
                } else {
                    when (vk) {
                        VK_SHIFT -> vk = VK_LSHIFT
                        VK_CONTROL -> vk = VK_LCONTROL
                        VK_MENU -> vk = VK_LMENU
                    }
                }
            }
            return SkikoKeyboardEvent(
                SkikoKey.valueOf(vk),
                kind = kind,
                platform = this
            )
        }
}

actual data class SkikoPlatformInputEvent(
    val charCode: WPARAM,
    val modifiers: LPARAM,
    val inputModifiers: SkikoInputModifiers
) {
    val skikoEvent: SkikoInputEvent
        get() {
            var vk = LOWORD(charCode.toLong())
            if (vk == VK_SHIFT.toUInt() || vk == VK_CONTROL.toUInt() || vk == VK_MENU.toUInt()) {
                val keyFlags = HIWORD(modifiers)
                var scanCode = LOBYTE(keyFlags.toLong())
                val isExtendedKey = (keyFlags and KF_EXTENDED.toUInt()) == KF_EXTENDED.toUInt()
                if (isExtendedKey) {
                    scanCode = MAKEWORD(scanCode, 0xE0u)
                }
                val code = MapVirtualKeyA(scanCode, MAPVK_VK_TO_VSC_EX)
                vk = LOWORD(code.toLong())
            }
            return SkikoInputEvent(
                charCode.toInt().toChar().toString(),
                key = SkikoKey.valueOf(vk.toInt()),
                kind = SkikoKeyboardEventKind.TYPE,
                platform = this,
            )
        }
}

actual data class SkikoPlatformPointerEvent(
    val kind: SkikoPointerEventKind,
    val pressedButtons: WPARAM,
    val position: LPARAM,
    val button: SkikoMouseButtons = SkikoMouseButtons.NONE,
    val inputModifiers: SkikoInputModifiers = SkikoInputModifiers.EMPTY,
) {
    val skikoEvent: SkikoPointerEvent
        get() {
            val x = LOWORD(position)
            val y = HIWORD(position)
            var pressed = SkikoMouseButtons.NONE
            if (pressedButtons and MK_LBUTTON.toULong() != 0uL) {
                pressed += SkikoMouseButtons.LEFT
            }
            if (pressedButtons and MK_RBUTTON.toULong() != 0uL) {
                pressed += SkikoMouseButtons.RIGHT
            }
            if (pressedButtons and MK_MBUTTON.toULong() != 0uL) {
                pressed += SkikoMouseButtons.MIDDLE
            }

            val deltaX: Double = when {
                kind == SkikoPointerEventKind.SCROLL && inputModifiers.has(SkikoInputModifiers.SHIFT) -> HIWORD(
                    pressedButtons.toLong()
                ).toShort().toDouble() / 120.0

                else -> 0.0
            }

            val deltaY: Double = when {
                kind == SkikoPointerEventKind.SCROLL && !inputModifiers.has(SkikoInputModifiers.SHIFT) -> HIWORD(
                    pressedButtons.toLong()
                ).toShort().toDouble() / 120.0

                else -> 0.0
            }

            return SkikoPointerEvent(
                x = x.toDouble(),
                y = y.toDouble(),
                deltaX = deltaX,
                deltaY = deltaY,
                pressedButtons = pressed,
                button = button,
                kind = kind,
                modifiers = inputModifiers,
                platform = this
            )
        }
}

actual typealias SkikoTouchPlatformEvent = Any
actual typealias SkikoGesturePlatformEvent = Any
