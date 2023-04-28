package org.jetbrains.skiko

import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.UIEvent

internal actual fun getEventTimestamp(e: UIEvent): Long {
    // TODO: why can't do timestamp.toLong()
    return e.timeStamp.toInt().toLong()
}