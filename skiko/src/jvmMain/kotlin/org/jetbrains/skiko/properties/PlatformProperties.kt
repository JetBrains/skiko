package org.jetbrains.skiko.properties

import java.awt.Component

internal interface PlatformProperties {
    fun isFullscreen(component: Component): Boolean
    fun makeFullscreen(component: Component, value: Boolean)
}