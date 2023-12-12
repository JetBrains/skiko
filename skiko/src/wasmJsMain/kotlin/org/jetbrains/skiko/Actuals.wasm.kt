package org.jetbrains.skiko

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.UIEvent

internal fun getEventTimestamp(e: UIEvent): Long {
    // TODO: why can't do timestamp.toLong()
    return e.timeStamp.toInt().toLong()
}

internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    if (component is HTMLElement) {
        component.style.cursor = cursor
    }
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? {
    return if (component is HTMLElement) {
        component.style.cursor
    } else {
        null
    }
}

internal actual fun getNavigatorInfo(): String =
    js("navigator.userAgentData ? navigator.userAgentData.platform : navigator.platform")