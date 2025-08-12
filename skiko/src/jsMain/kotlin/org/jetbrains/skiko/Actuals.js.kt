package org.jetbrains.skiko

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.UIEvent

internal fun getEventTimestamp(e: UIEvent): Long {
    return e.timeStamp.toLong()
}

internal actual fun CursorManager_setCursor(component: Any, cursor: Cursor) {
    (component as? HTMLElement)?.style?.cursor = cursor
}

internal actual fun CursorManager_getCursor(component: Any): Cursor? {
    return (component as? HTMLElement)?.style?.cursor
}

internal actual fun getNavigatorInfo(): String =
    js("navigator.userAgentData ? navigator.userAgentData.platform : navigator.platform") as String

internal actual external val GL: GLInterface