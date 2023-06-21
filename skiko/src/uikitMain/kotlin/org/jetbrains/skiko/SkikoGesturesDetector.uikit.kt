package org.jetbrains.skiko

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.*

internal expect fun skikoCreateGesturesDetectors(layer: SkiaLayer): List<() -> Map<SkikoGestureEventKind, List<UIGestureRecognizer>>>

// See https://developer.apple.com/documentation/uikit/touches_presses_and_gestures/using_responders_and_the_responder_chain_to_handle_events?language=objc
internal class SkikoGesturesDetectorUikit(
    private val layer: SkiaLayer
) : NSObject() {

    private val view: UIView?
        get() = layer.view

    @ObjCAction
    fun onTap(sender: UITapGestureRecognizer) {
        val (x, y) = sender.locationInView(view).useContents { x to y }
        // println("onTap: $x,$y")
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
        // println("onDoubleTap: $x,$y")
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
        // println("onLongPress: $x,$y")
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
                scale = sender.scale(),
                velocity = sender.velocity(),
                state = toSkikoGestureState(sender.state)
            )
        )
    }

    @ObjCAction
    fun onSwipe(sender: UISwipeGestureRecognizer) {
        val (x, y) = sender.locationInView(view).useContents { x to y }
        // println("onSwipe: $x,$y, dir: ${sender.direction}")
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
        // println("onPan: $x,$y")
        layer.skikoView?.onGestureEvent(
            SkikoGestureEvent(
                x = x,
                y = y,
                kind = SkikoGestureEventKind.PAN,
                state = toSkikoGestureState(sender.state)
            )
        )
    }

    val gestureRecognizers = hashMapOf(
        SkikoGestureEventKind.TAP to listOf(
            UITapGestureRecognizer(this, NSSelectorFromString("onTap:"))
        ),
        SkikoGestureEventKind.DOUBLETAP to listOf(
            UITapGestureRecognizer(this, NSSelectorFromString("onDoubleTap:")).apply {
                numberOfTapsRequired = 2.toULong()
            }),
        SkikoGestureEventKind.LONGPRESS to listOf(
            UILongPressGestureRecognizer(this, NSSelectorFromString("onLongPress:"))
        ),
        SkikoGestureEventKind.PINCH to listOf(
            UIPinchGestureRecognizer(this, NSSelectorFromString("onPinch:"))
        ),
        SkikoGestureEventKind.SWIPE to listOf(
            // UISwipeGestureRecognizer can recognize or-ed directions, but it just returns what was configured, not what was recognized.  
            // So we need a recognizer per direction 
            UISwipeGestureRecognizer(this, NSSelectorFromString("onSwipe:")).apply {
                direction = UISwipeGestureRecognizerDirectionLeft
            },
            UISwipeGestureRecognizer(this, NSSelectorFromString("onSwipe:")).apply {
                direction = UISwipeGestureRecognizerDirectionRight
            },
            UISwipeGestureRecognizer(this, NSSelectorFromString("onSwipe:")).apply {
                direction = UISwipeGestureRecognizerDirectionUp
            },
            UISwipeGestureRecognizer(this, NSSelectorFromString("onSwipe:")).apply {
                direction = UISwipeGestureRecognizerDirectionDown
            },
        ),
        // TODO: SWIPE and PAN don't work together by default, see https://stackoverflow.com/q/5111828/18575
        SkikoGestureEventKind.PAN to listOf(
            UIPanGestureRecognizer(this, NSSelectorFromString("onPan:"))
        )
    )
}
