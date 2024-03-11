package org.jetbrains.skiko

import kotlinx.browser.window
import org.w3c.dom.AddEventListenerOptions
import org.w3c.dom.MediaQueryListEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.UIEvent

actual typealias SkikoGesturePlatformEvent = Any
actual typealias SkikoPlatformInputEvent = KeyboardEvent
actual typealias SkikoPlatformKeyboardEvent = KeyboardEvent
//  MouseEvent is base class of PointerEvent
actual typealias SkikoPlatformPointerEvent = UIEvent

internal actual fun SkiaLayer.setOnChangeScaleNotifier() {
    state?.initCanvas(desiredWidth, desiredHeight, contentScale, this.pixelGeometry)
    window.matchMedia("(resolution: ${contentScale}dppx)")
        .addEventListener("change", { evt ->
            evt as MediaQueryListEvent
            if (!evt.matches) {
                setOnChangeScaleNotifier()
            }
        }, AddEventListenerOptions(capture = true, once = true))
    onContentScaleChanged?.invoke(contentScale)
}