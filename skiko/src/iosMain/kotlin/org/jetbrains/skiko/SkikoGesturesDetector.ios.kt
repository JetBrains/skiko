package org.jetbrains.skiko

import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.darwin.NSObject
import platform.Foundation.NSSelectorFromString
import platform.UIKit.*

// See https://developer.apple.com/documentation/uikit/touches_presses_and_gestures/using_responders_and_the_responder_chain_to_handle_events?language=objc
internal class SkikoGesturesDetector(
    private val layer: SkiaLayer
) : NSObject() {

    val view: UIView?
        get() = layer.view

    @ObjCAction
    fun onTap(sender: UITapGestureRecognizer) {
        val (x, y) = sender.locationInView(view).useContents { x to y }
        layer.skikoView?.onGestureEvent(
            SkikoGestureEvent(
                x = x,
                y = y,
                kind = SkikoGestureEventKind.TAP,
                state = toSkikoGestureState(sender.state)
            )
        )
    }
    @ObjCAction
    fun onDoubleTap(sender: UITapGestureRecognizer) {
        val (x, y) = sender.locationInView(view).useContents { x to y }
        layer.skikoView?.onGestureEvent(
            SkikoGestureEvent(
                x = x,
                y = y,
                kind = SkikoGestureEventKind.DOUBLETAP,
                state = toSkikoGestureState(sender.state)
            )
        )
    }
    @ObjCAction
    fun onLongPress(sender: UILongPressGestureRecognizer) {
        val (x, y) = sender.locationInView(view).useContents { x to y }
        layer.skikoView?.onGestureEvent(
            SkikoGestureEvent(
                x = x,
                y = y,
                kind = SkikoGestureEventKind.LONGPRESS,
                state = toSkikoGestureState(sender.state)
            )
        )
    }
    @ObjCAction
    fun onPinch(sender: UIPinchGestureRecognizer) {
        val (x, y) = sender.locationInView(view).useContents { x to y }
        layer.skikoView?.onGestureEvent(
            SkikoGestureEvent(
                x = x,
                y = y,
                kind = SkikoGestureEventKind.PINCH,
                scale = sender.scale,
                velocity = sender.velocity,
                state = toSkikoGestureState(sender.state)
            )
        )
    }
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
    @ObjCAction
    fun onSwipe(sender: UISwipeGestureRecognizer) {
        val (x, y) = sender.locationInView(view).useContents { x to y }
        layer.skikoView?.onGestureEvent(
            SkikoGestureEvent(
                x = x,
                y = y,
                kind = SkikoGestureEventKind.SWIPE,
                direction = toSkikoGestureDirection(sender.direction),
                state = toSkikoGestureState(sender.state)
            )
        )
    }
    @ObjCAction
    fun onPan(sender: UIPanGestureRecognizer) {
        val (x, y) = sender.locationInView(view).useContents { x to y }
        layer.skikoView?.onGestureEvent(
            SkikoGestureEvent(
                x = x,
                y = y,
                kind = SkikoGestureEventKind.PAN,
                state = toSkikoGestureState(sender.state)
            )
        )
    }

    // We have ':' in selector to take care of function argument.
    private val gestureRecognizers = hashMapOf(
        SkikoGestureEventKind.TAP to UITapGestureRecognizer(this, NSSelectorFromString("onTap:")),
        SkikoGestureEventKind.DOUBLETAP to UITapGestureRecognizer(this, NSSelectorFromString("onDoubleTap:")).apply {
            numberOfTapsRequired = 2.toULong()
        },
        SkikoGestureEventKind.LONGPRESS to UILongPressGestureRecognizer(this, NSSelectorFromString("onLongPress:")),
        SkikoGestureEventKind.PINCH to UIPinchGestureRecognizer(this, NSSelectorFromString("onPinch:")),
        SkikoGestureEventKind.ROTATION to UIRotationGestureRecognizer(this, NSSelectorFromString("onRotation:")),
        SkikoGestureEventKind.SWIPE to UISwipeGestureRecognizer(this, NSSelectorFromString("onSwipe:")),
        SkikoGestureEventKind.PAN to UIPanGestureRecognizer(this, NSSelectorFromString("onPan:"))
    )

    fun setGesturesToListen(gestures: Array<SkikoGestureEventKind>?) {
        clearGesturesToListen()
        if (!gestures.isNullOrEmpty()) {
            for (gesture in gestures) {
                if (gestureRecognizers.containsKey(gesture)) {
                    view?.addGestureRecognizer(gestureRecognizers.get(gesture)!!)
                }
            }
        }
    }

    private fun clearGesturesToListen() {
        for ((key, value) in gestureRecognizers) {
            view?.removeGestureRecognizer(value)
        }
    }
}
