package org.jetbrains.skiko

import platform.UIKit.*

fun toSkikoGestureDirection(direction: UISwipeGestureRecognizerDirection) : SkikoGestureEventDirection {
    return when(direction) {
        UISwipeGestureRecognizerDirectionUp -> SkikoGestureEventDirection.UP
        UISwipeGestureRecognizerDirectionDown -> SkikoGestureEventDirection.DOWN
        UISwipeGestureRecognizerDirectionLeft -> SkikoGestureEventDirection.LEFT
        UISwipeGestureRecognizerDirectionRight -> SkikoGestureEventDirection.RIGHT
        else -> SkikoGestureEventDirection.UNKNOWN
    }
}

fun toSkikoGestureState(state: UIGestureRecognizerState ) : SkikoGestureEventState  {
    return when(state) {
        UIGestureRecognizerStatePossible -> SkikoGestureEventState.PRESSED
        UIGestureRecognizerStateBegan -> SkikoGestureEventState.STARTED
        UIGestureRecognizerStateChanged -> SkikoGestureEventState.CHANGED
        UIGestureRecognizerStateEnded -> SkikoGestureEventState.ENDED
        else -> SkikoGestureEventState.UNKNOWN
    }
}
