package org.jetbrains.skiko

import platform.UIKit.*

internal actual fun UIView.skikoInitializeUIView() {
    userInteractionEnabled = true
}
