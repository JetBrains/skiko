package org.jetbrains.skiko

import platform.UIKit.*

internal actual fun UIView.skikoInitializeUIView() {
    multipleTouchEnabled = true
    userInteractionEnabled = true
}
