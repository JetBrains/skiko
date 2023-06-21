package org.jetbrains.skiko

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.*

internal actual fun skikoCreateGesturesDetectors(layer: SkiaLayer): List<() -> Map<SkikoGestureEventKind, List<UIGestureRecognizer>>> {
    return listOf(
        SkikoGesturesDetectorUikit(layer)::gestureRecognizers,
        SkikoGesturesDetectorIos(layer)::gestureRecognizers
    )
}

internal class SkikoGesturesDetectorIos(private val layer: SkiaLayer) : NSObject() {
    private val view: UIView?
        get() = layer.view

    @ObjCAction
    fun onRotation(sender: UIRotationGestureRecognizer) {
        val (x, y) = sender.locationInView(view).useContents { x to y }
        layer.skikoView?.onGestureEvent(
            SkikoGestureEvent(
                x = x,
                y = y,
                kind = SkikoGestureEventKind.ROTATION,
                rotation = sender.rotation,
                velocity = sender.velocity,
                state = toSkikoGestureState(sender.state)
            )
        )
    }

    val gestureRecognizers = hashMapOf(
        SkikoGestureEventKind.ROTATION to listOf(
            UIRotationGestureRecognizer(this, NSSelectorFromString("onRotation:"))
        )
    )
}
