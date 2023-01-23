package org.jetbrains.skiko.sample

import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.jetbrains.skiko.SkiaLayer
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGImageAlphaInfo
import platform.Metal.MTLDeviceProtocol
import platform.UIKit.UIView

class SwiftHelper {
    fun getViewController() = getSkikoViewContoller()
}
