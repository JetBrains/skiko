package org.jetbrains.skiko

internal actual fun getNavigatorInfo(): String =
    js("navigator.userAgentData ? navigator.userAgentData.platform : navigator.platform") as String

internal actual external val GL: GLInterface
