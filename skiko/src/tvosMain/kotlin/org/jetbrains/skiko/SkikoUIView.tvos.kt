package org.jetbrains.skiko

import org.jetbrains.skia.*

internal actual fun SkikoUIView.skikoInitializeUIView() {
    userInteractionEnabled = true
}

internal actual fun SkikoUIView.skikoShowTextMenu(targetRect: Rect) {
}

internal actual fun SkikoUIView.skikoHideTextMenu() {
}