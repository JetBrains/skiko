package org.jetbrains.skiko

import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.UIEvent

internal actual fun getEventTimestamp(e: UIEvent): Long {
    return e.timeStamp.toLong()
}