package org.jetbrains.skiko

import org.w3c.dom.events.UIEvent

internal actual fun getNavigatorInfo(): String =
    js("navigator.userAgentData ? navigator.userAgentData.platform : navigator.platform")
