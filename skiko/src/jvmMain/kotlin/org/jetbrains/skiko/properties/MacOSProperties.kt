package org.jetbrains.skiko.properties

import java.awt.Component
import org.jetbrains.skiko.properties.PlatformProperties

internal class MacOSProperties : PlatformProperties {

    external override fun isFullscreen(component: Component): Boolean
    
    external override fun makeFullscreen(component: Component, value: Boolean)
}