package org.jetbrains.skiko

import org.jetbrains.skia.*
import platform.CoreGraphics.*
import platform.UIKit.*

internal actual fun SkikoUIView.skikoInitializeUIView() {
    multipleTouchEnabled = true
    userInteractionEnabled = true
}

internal actual fun SkikoUIView.skikoShowTextMenu(targetRect: Rect) {
    val menu: UIMenuController = UIMenuController.sharedMenuController()
    if (menu.isMenuVisible()) {
        menu.hideMenu()
    }
    val cgRect = CGRectMake(
        x = targetRect.left.toDouble(),
        y = targetRect.top.toDouble(),
        width = targetRect.width.toDouble(),
        height = targetRect.height.toDouble()
    )
    menu.showMenuFromView(this, cgRect)
}

internal actual fun SkikoUIView.skikoHideTextMenu() {
    val menu: UIMenuController = UIMenuController.sharedMenuController()
    menu.hideMenu()
}