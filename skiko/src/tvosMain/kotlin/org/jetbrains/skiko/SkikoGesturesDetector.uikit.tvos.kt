package org.jetbrains.skiko

import platform.UIKit.*

internal actual fun skikoCreateGesturesDetectors(layer: SkiaLayer): List<() -> Map<SkikoGestureEventKind, List<UIGestureRecognizer>>> {
    return listOf(SkikoGesturesDetectorUikit(layer)::gestureRecognizers)
}