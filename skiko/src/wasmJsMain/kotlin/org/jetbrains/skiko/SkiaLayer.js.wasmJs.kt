package org.jetbrains.skiko

import kotlinx.browser.window

import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.UIEvent


actual typealias SkikoGesturePlatformEvent = Any
actual typealias SkikoPlatformInputEvent = KeyboardEvent
actual typealias SkikoPlatformKeyboardEvent = KeyboardEvent
actual typealias SkikoPlatformPointerEvent = UIEvent

internal actual fun SkiaLayer.setOnChangeScaleNotifier() {
    state?.initCanvas(desiredWidth, desiredHeight, contentScale, this.pixelGeometry)
    window.matchMedia("(resolution: ${contentScale}dppx)")
        .addEventListener("change", { setOnChangeScaleNotifier() }, true)
    onContentScaleChanged?.invoke(contentScale)
}